package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.LastDecisionResponse;
import lion.mode.tradebot_backend.model.WalkForwardReport;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.repository.WalkForwardReportRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DecisionMatrixService {

    private final StockDataRepository stockDataRepository;
    private final WalkForwardReportRepository walkForwardReportRepository;
    private final WalkForwardOptimizationService walkforwardService;
    private Gson gson = new Gson();

    public LastDecisionResponse lastDecisionGenerator(WalkForwardRequestDto requestDto){
        LastDecisionResponse decisionMatrixDto = new LastDecisionResponse();
        decisionMatrixDto.setSymbol(requestDto.getSymbol().toUpperCase());

        WalkForwardReport walkforwardReport = walkforwardService.runWalkForwardAnalysis(requestDto);

        String strategyName = walkforwardReport.getStrategyName();
        String parameters = gson.toJson(walkforwardReport.getParameters());
        decisionMatrixDto.setStrategyName(strategyName);
        decisionMatrixDto.setParameters(parameters);
        decisionMatrixDto.setDate(walkforwardReport.getEndDate());

        try{
            decisionMatrixDto.setConfidence(walkforwardReport.getConfidence());
            String signal = walkforwardReport.getLastSignal();
            decisionMatrixDto.setScore(scoreGeneratorWithConfidence(signal, walkforwardReport.getTotalProfitLossRatioPercent()));
            decisionMatrixDto.setLastSignal(signalGeneratorByScore(decisionMatrixDto.getScore()));
            System.out.println("[DONE] Walkforward analysis completed successfully.");
            walkForwardReportRepository.save(walkforwardReport);
        } catch (Exception e){
            System.err.println("[ERROR] Error during walkforward analysis: " + e.getMessage());
        }
        return decisionMatrixDto;
    }

    public List<LastDecisionResponse> lastDecisionResponseForAllSymbols(WalkForwardRequestDto requestDto) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<LastDecisionResponse> responses = new ArrayList<>();

        List<WalkForwardRequestDto.IndicatorParam> baseIndicators = new ArrayList<>();
        if (requestDto.getIndicators() != null) {
            for (WalkForwardRequestDto.IndicatorParam p : requestDto.getIndicators()) {
                baseIndicators.add(new WalkForwardRequestDto.IndicatorParam(p.getType(), p.getParams()));
            }
        }

        for (String symbol : stockDataRepository.findSymbols()) {
            WalkForwardRequestDto fullRequestDto = new WalkForwardRequestDto();
            fullRequestDto.setInterval(requestDto.getInterval());
            fullRequestDto.setOptimizationWindow(requestDto.getOptimizationWindow());
            fullRequestDto.setValidationWindow(requestDto.getValidationWindow());
            fullRequestDto.setRollStep(requestDto.getRollStep());
            fullRequestDto.setSymbol(symbol);
            fullRequestDto.setIndicators(new ArrayList<>(baseIndicators));
            responses.add(lastDecisionGenerator(fullRequestDto));
        }
        
        System.gc();
        stopWatch.stop();
        System.out.println("Total time for processing all symbols: " + stopWatch.getTime() / 1000 + " s");
        stopWatch.reset();

        return responses;
    }

    // Helpers

    private double scoreGeneratorWithConfidence(String signal, double confidence){
        int score;
        if (signal.equalsIgnoreCase("BUY") || signal.equalsIgnoreCase("STRONG_BUY")) score = 1;
        else if (signal.equalsIgnoreCase("SELL") || signal.equalsIgnoreCase("STRONG_SELL")) score = -1;
        else score = 0;
        return Math.tanh(score) * (confidence / 100);
    }

    private String signalGeneratorByScore(double score) {
        if (score >= 0.5) return "STRONG_BUY";
        else if (score > 0 && score < 0.5) return "BUY";
        else if (score >= -0.1 && score <= 0.1) return "NEUTRAL";
        else if (score >= -0.5 && score < 0) return "SELL";
        else return "STRONG_SELL";
    }

}
