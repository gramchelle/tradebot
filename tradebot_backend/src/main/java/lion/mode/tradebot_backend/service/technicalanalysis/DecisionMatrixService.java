package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto;
import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDtoNoSymbol;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.AllSymbolsBacktestResult;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.DecisionMatrixDto;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.LastDecisionResponse;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.StrategyBacktestDto;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.WalkForwardReport;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.repository.WalkForwardReportRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.TrailingStopLossRule;

import com.google.gson.Gson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DecisionMatrixService {

    private final StrategyService strategyService;
    private final BacktestRepository backtestRepository;
    private final StockDataRepository stockDataRepository;
    private final WalkForwardReportRepository walkForwardReportRepository;
    private final WalkForwardOptimizationService walkforwardService;
    private Gson gson = new Gson();
    private StopWatch stopWatch = new StopWatch();

    public StrategyBacktestDto calculateMovingAverages(String symbol, String source, double stopLoss, double takeProfit, Map<String, Object> params, int lookback) {
        symbol = symbol.toUpperCase().trim();
        if (stockDataRepository.findTopBySymbolOrderByTimestampDesc(symbol).isEmpty()) throw new NotEnoughDataException("No data found for symbol: " + symbol);
        BarSeries fullSeries = strategyService.loadSeries(symbol);
        BarSeries series = (lookback > 0) ? strategyService.sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        Indicator<Num> prices = strategyService.sourceSelector(source, series);

        Strategy sma10 = RuleGeneratorFactory.simpleSmaStrategy(series, prices, Map.of("indicator", "SMA", "period", 10));
        Strategy sma20 = RuleGeneratorFactory.simpleSmaStrategy(series, prices, Map.of("indicator", "SMA", "period", 20));
        Strategy sma30 = RuleGeneratorFactory.simpleSmaStrategy(series, prices, Map.of("indicator", "SMA", "period", 30));
        Strategy sma50 = RuleGeneratorFactory.simpleSmaStrategy(series, prices, Map.of("indicator", "SMA", "period", 50));
        Strategy sma100 = RuleGeneratorFactory.simpleSmaStrategy(series, prices, Map.of("indicator", "SMA", "period", 100));
        Strategy sma200 = RuleGeneratorFactory.simpleSmaStrategy(series, prices, Map.of("indicator", "SMA", "period", 200));

        Strategy ema10 = RuleGeneratorFactory.simpleEmaStrategy(series, prices, Map.of("indicator", "EMA", "period", 10));
        Strategy ema20 = RuleGeneratorFactory.simpleEmaStrategy(series, prices, Map.of("indicator", "EMA", "period", 20));
        Strategy ema30 = RuleGeneratorFactory.simpleEmaStrategy(series, prices, Map.of("indicator", "EMA", "period", 30));
        Strategy ema50 = RuleGeneratorFactory.simpleEmaStrategy(series, prices, Map.of("indicator", "EMA", "period", 50));
        Strategy ema100 = RuleGeneratorFactory.simpleEmaStrategy(series, prices, Map.of("indicator", "EMA", "period", 100));
        Strategy ema200 = RuleGeneratorFactory.simpleEmaStrategy(series, prices, Map.of("indicator", "EMA", "period", 200));

        StopLossRule stopLossRule = new StopLossRule(new ClosePriceIndicator(series), DecimalNum.valueOf(stopLoss));
        StopGainRule takeProfitRule = new StopGainRule(new ClosePriceIndicator(series), DecimalNum.valueOf(takeProfit));
        //TrailingStopLossRule trailingStopLossRule = new TrailingStopLossRule(new ClosePriceIndicator(series), DecimalNum.valueOf(stopLoss));

        // Kombine strateji
        Rule entry = sma10.getEntryRule().or(sma20.getEntryRule())
                .or(sma30.getEntryRule())
                .or(sma50.getEntryRule())
                .or(sma100.getEntryRule())
                .or(sma200.getEntryRule())
                .or(ema10.getEntryRule())
                .or(ema20.getEntryRule())
                .or(ema30.getEntryRule())
                .or(ema50.getEntryRule())
                .or(ema100.getEntryRule())
                .or(ema200.getEntryRule());

        Rule exit = sma10.getExitRule().or(sma20.getExitRule()
                .or(sma30.getExitRule())
                .or(sma50.getExitRule())
                .or(sma100.getExitRule())
                .or(sma200.getExitRule())
                .or(ema10.getExitRule())
                .or(ema20.getExitRule())
                .or(ema30.getExitRule())
                .or(ema50.getExitRule())
                .or(ema100.getExitRule())
                .or(ema200.getExitRule())
                .or(takeProfitRule)
                .or(stopLossRule));
                //.or(trailingStopLossRule));

        Strategy combined = new BaseStrategy("Moving Averages Strategy", entry, exit);
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(combined);

        StrategyBacktestDto backtestDto = strategyService.calculatePerformanceMetrics(symbol, combined, series, record, lookback);

        // Tek JSON param kaydı
        Map<String, Object> allParams = new HashMap<>();
        allParams.put("SMA10", Map.of("period", 10));
        allParams.put("SMA20", Map.of("period", 20));
        allParams.put("SMA30", Map.of("period", 30));
        allParams.put("SMA50", Map.of("period", 50));
        allParams.put("SMA100", Map.of("period", 100));
        allParams.put("SMA200", Map.of("period", 200));
        allParams.put("EMA10", Map.of("period", 10));
        allParams.put("EMA20", Map.of("period", 20));
        allParams.put("EMA30", Map.of("period", 30));
        allParams.put("EMA50", Map.of("period", 50));
        allParams.put("EMA100", Map.of("period", 100));
        allParams.put("EMA200", Map.of("period", 200));
        allParams.put("stopLoss", stopLoss);
        allParams.put("takeProfit", takeProfit);
        allParams.put("lookback", lookback);
        backtestDto.setParametersJson(gson.toJson(allParams));

        try {
            backtestRepository.save(strategyService.backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + combined.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto calculateDecisionMatrix(String symbol, String source, double stopLoss, double takeProfit, Map<String, Object> params, int lookback) {
        symbol = symbol.toUpperCase();
        if (stockDataRepository.findTopBySymbolOrderByTimestampDesc(symbol).isEmpty()) throw new NotEnoughDataException("No data found for symbol: " + symbol);

        BarSeries fullSeries = strategyService.loadSeries(symbol);
        BarSeries series = (lookback > 0) ? strategyService.sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        Indicator<Num> prices = strategyService.sourceSelector(source, series);

        int rsiPeriod = (int) params.getOrDefault("rsiPeriod", 14);
        int mfiPeriod = (int) params.getOrDefault("mfiPeriod", 14);
        int dmiPeriod = (int) params.getOrDefault("dmiPeriod", 14);
        int adxThreshold = (int) params.getOrDefault("adxThreshold", 25);
        int smaShort = (int) params.getOrDefault("smaPeriod", 50);
        int smaLongPeriod = (int) params.getOrDefault("smaLongPeriod", 200);
        int maShort = (int) params.getOrDefault("emaShort", 9);
        int maLong = (int) params.getOrDefault("emaLong", 21);
        int macdShort = (int) params.getOrDefault("macdShort", 12);
        int macdLong = (int) params.getOrDefault("macdLong", 26);
        int signalPeriod = (int) params.getOrDefault("signalPeriod", 9);
        int bbPeriod = (int) params.getOrDefault("bbPeriod", 20);
        double bbStdDev = (double) params.getOrDefault("bbStdDev", 2.0);

        Map<String, Object> dmiParams = Map.of("indicator", "DMI", "period", dmiPeriod, "adxThreshold", adxThreshold);
        Strategy dmi = RuleGeneratorFactory.buildDmiStrategy(series, prices, dmiParams);

        Map<String, Object> emaParams = Map.of("indicator", "EMA Crossover", "shortPeriod", maShort, "longPeriod", maLong);
        Strategy emaCrossover = RuleGeneratorFactory.buildEMACrossoverStrategy(series, prices, emaParams);

        Map<String, Object> mfiParams = Map.of("indicator", "MFI", "period", mfiPeriod);
        Strategy mfi = RuleGeneratorFactory.buildMfiStrategy(series, prices, mfiParams);

        Map<String, Object> bbParams = Map.of("indicator", "Bollinger Bands", "period", bbPeriod, "stdDev", bbStdDev);
        Strategy bb = RuleGeneratorFactory.buildBollingerBandsStrategy(series, prices, bbParams);

        Map<String, Object> rsiParams = Map.of("indicator", "RSI", "period", rsiPeriod);
        Strategy rsi = RuleGeneratorFactory.buildRsiStrategy(series, prices, rsiParams);

        Map<String, Object> macdParams = Map.of(
                "indicator", "MACD",
                "shortPeriod", macdShort,
                "longPeriod", macdLong,
                "signalPeriod", signalPeriod
        );
        Strategy macd = RuleGeneratorFactory.buildMacdStrategy(series, prices, macdParams);

        Map<String, Object> smaParams = Map.of("indicator", "SMA", "period", smaShort);
        Strategy sma50 = RuleGeneratorFactory.simpleSmaStrategy(series, prices, smaParams);

        Map<String, Object> sma200Params = Map.of("indicator", "SMA200", "period", smaLongPeriod);
        Strategy sma200 = RuleGeneratorFactory.simpleSmaStrategy(series, prices, sma200Params);

        StopLossRule stopLossRule = new StopLossRule(new ClosePriceIndicator(series), DecimalNum.valueOf(stopLoss));
        StopGainRule takeProfitRule = new StopGainRule(new ClosePriceIndicator(series), DecimalNum.valueOf(takeProfit));
        TrailingStopLossRule trailingStopLossRule = new TrailingStopLossRule(new ClosePriceIndicator(series), DecimalNum.valueOf(stopLoss));

        if (stopLoss > 0) {
            dmi = new BaseStrategy(dmi.getName(), dmi.getEntryRule(), dmi.getExitRule().or(stopLossRule).or(trailingStopLossRule));
            emaCrossover = new BaseStrategy(emaCrossover.getName(), emaCrossover.getEntryRule(), emaCrossover.getExitRule().or(stopLossRule).or(trailingStopLossRule));
            mfi = new BaseStrategy(mfi.getName(), mfi.getEntryRule(), mfi.getExitRule().or(stopLossRule).or(trailingStopLossRule));
            bb = new BaseStrategy(bb.getName(), bb.getEntryRule(), bb.getExitRule().or(stopLossRule).or(trailingStopLossRule));
            rsi = new BaseStrategy(rsi.getName(), rsi.getEntryRule(), rsi.getExitRule().or(stopLossRule).or(trailingStopLossRule));
            macd = new BaseStrategy(macd.getName(), macd.getEntryRule(), macd.getExitRule().or(stopLossRule).or(trailingStopLossRule));
            sma50 = new BaseStrategy(sma50.getName(), sma50.getEntryRule(), sma50.getExitRule().or(stopLossRule).or(trailingStopLossRule));
            sma200 = new BaseStrategy(sma200.getName(), sma200.getEntryRule(), sma200.getExitRule().or(stopLossRule).or(trailingStopLossRule));
        }

        Rule entry = dmi.getEntryRule().or(emaCrossover.getEntryRule())
                .or(mfi.getEntryRule())
                .or(bb.getEntryRule())
                .or(rsi.getEntryRule())
                .or(macd.getEntryRule())
                .or(sma50.getEntryRule())
                .or(sma200.getEntryRule());

        Rule exit = dmi.getExitRule().or(emaCrossover.getExitRule()
                .or(mfi.getExitRule())
                .or(bb.getExitRule())
                .or(rsi.getExitRule())
                .or(macd.getExitRule())
                .or(sma50.getExitRule())
                .or(sma200.getExitRule())
                .or(takeProfitRule)
                .or(stopLossRule)
                .or(trailingStopLossRule));

        Strategy combined = new BaseStrategy("Decision Matrix Strategy", entry, exit);
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(combined);

        StrategyBacktestDto backtestDto = strategyService.calculatePerformanceMetrics(symbol, combined, series, record, lookback);

        Map<String, Object> allParams = new HashMap<>();
        allParams.put("DMI", dmiParams);
        allParams.put("EMA", emaParams);
        allParams.put("MFI", mfiParams);
        allParams.put("BollingerBands", bbParams);
        allParams.put("RSI", rsiParams);
        allParams.put("MACD", macdParams);
        allParams.put("SMA50", smaParams);
        allParams.put("SMA200", sma200Params);
        backtestDto.setParametersJson(gson.toJson(allParams));

        try {
            backtestRepository.save(strategyService.backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + combined.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public List<AllSymbolsBacktestResult> runForAllSymbols(String source, double stopLoss, double takeProfit, Map<String, Object> params, int lookback){
        List<AllSymbolsBacktestResult> results = new ArrayList<>();
        for (String symbol : stockDataRepository.findSymbols()){
            try {
                StrategyBacktestDto response = calculateDecisionMatrix(symbol, source, stopLoss, takeProfit, params, lookback);
                AllSymbolsBacktestResult result = new AllSymbolsBacktestResult();
                result.setSymbol(symbol);
                result.setStrategyName(response.getStrategyName());
                result.setSignal(response.getCurrentSignal());
                result.setTotalProfitLossRatioPercent(response.getTotalProfit());
                results.add(result);
            } catch (Exception e) {
                System.out.println("Error running backtest for " + symbol + ": " + e.getMessage());
            }
        }
        return results;
    }

    public AllSymbolsBacktestResult runOverallTechnicalSignal(String symbol, String source, double stopLoss, double takeProfit, Map<String, Object> params, int lookback){
        symbol = symbol.toUpperCase();
        StrategyBacktestDto calStrategyBacktestDto = calculateDecisionMatrix(symbol, source, stopLoss, takeProfit, params, lookback);
        StrategyBacktestDto response = calculateMovingAverages(symbol, source, stopLoss, takeProfit, params, lookback);
        AllSymbolsBacktestResult result = new AllSymbolsBacktestResult();
        result.setSymbol(symbol);
        result.setStrategyName("Technical Analysis");
        result.setSignal(calStrategyBacktestDto.getCurrentSignal());
        result.setTotalProfitLossRatioPercent(calStrategyBacktestDto.getTotalProfit());
        result.setMaResult(response.getCurrentSignal());
        result.setIndicatorResult(response.getCurrentSignal());
        return result;
    }

    public List<AllSymbolsBacktestResult> runTechnicalAnalysisForAllSymbols(String source, double stopLoss, double takeProfit, Map<String, Object> params, int lookback){
        List<AllSymbolsBacktestResult> results = new ArrayList<>();
        for (String symbol : stockDataRepository.findSymbols()){
            try {
                StrategyBacktestDto response = calculateDecisionMatrix(symbol, source, stopLoss, takeProfit, params, lookback);
                StrategyBacktestDto maResponse = calculateMovingAverages(symbol, source, stopLoss, takeProfit, params, lookback);
                AllSymbolsBacktestResult result = new AllSymbolsBacktestResult();
                result.setSymbol(symbol);
                result.setStrategyName("Technical Analysis");
                double ta_totalProfitLossRatioPercent = response.getTotalProfitLossRatioPercent() / 100;
                double ma_totalProfitLossRatioPercent = maResponse.getTotalProfitLossRatioPercent() / 100;
                result.setTotalProfitLossRatioPercent((ta_totalProfitLossRatioPercent + ma_totalProfitLossRatioPercent) / 2);

                double taSignalScore = strategyService.scoreGenerator(response.getCurrentSignal()) * ta_totalProfitLossRatioPercent;
                double maSignalScore = strategyService.scoreGenerator(maResponse.getCurrentSignal()) * ma_totalProfitLossRatioPercent;

                result.setMaResult(maResponse.getCurrentSignal());
                result.setIndicatorResult(maResponse.getCurrentSignal());

                result.setSignal(strategyService.signalGeneratorByScore((maSignalScore + taSignalScore) / 2));
                results.add(result);
            } catch (Exception e) {
                System.out.println("Error running backtest for " + symbol + ": " + e.getMessage());
            }
        }
        return results;
    }

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
            decisionMatrixDto.setScore(scoreGenerator(signal, walkforwardReport.getTotalProfitLossRatioPercent()));
            decisionMatrixDto.setLastSignal(signalGeneratorByScore(decisionMatrixDto.getScore()));
            System.out.println("[DONE] Walkforward analysis completed successfully.");
            walkForwardReportRepository.save(walkforwardReport);
        } catch (Exception e){
            System.err.println("[ERROR] Error during walkforward analysis: " + e.getMessage());
        }
        return decisionMatrixDto;
    }

    public List<LastDecisionResponse> lastDecisionResponseForAllSymbols(WalkForwardRequestDtoNoSymbol requestDto) {
        stopWatch.start();
        List<LastDecisionResponse> responses = new ArrayList<>();

        List<WalkForwardRequestDto.IndicatorParam> baseIndicators = new ArrayList<>();
        if (requestDto.getIndicators() != null) {
            for (WalkForwardRequestDtoNoSymbol.IndicatorParam p : requestDto.getIndicators()) {
                baseIndicators.add(new WalkForwardRequestDto.IndicatorParam(p.getType(), p.getParams()));
            }
        }

        for (String symbol : stockDataRepository.findSymbols()) {
            WalkForwardRequestDto fullRequestDto = new WalkForwardRequestDto();
            fullRequestDto.setStep(requestDto.getStep());
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

    private double scoreGenerator(String signal, double confidence){
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
