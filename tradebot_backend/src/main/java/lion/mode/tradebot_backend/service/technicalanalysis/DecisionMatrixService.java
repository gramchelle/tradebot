package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.base_responses.N_DecisionMatrixDto;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DecisionMatrixService {

    private final StrategyService strategyService;

    public N_DecisionMatrixDto getDecisionMatrix(String symbol, String source, int lookback) {
        N_DecisionMatrixDto decisionMatrixDto = new N_DecisionMatrixDto();
        decisionMatrixDto.setSymbol(symbol);
        decisionMatrixDto.setDate(Instant.now());
/*
        Map<String, Supplier<N_StrategyBacktestDto>> strategies = new HashMap<>();
        strategies.put("RSI", () -> strategyService.runRsiStrategyBacktest(symbol, source, 14, 70, 30, lookback));
        strategies.put("MACD", () -> strategyService.runMacdStrategyBacktest(symbol, source, 12, 26, 9, lookback));
        strategies.put("BollingerBands", () -> strategyService.runBollingerBandsStrategyBacktest(symbol, source, 20, 2, "SMA", lookback));
        strategies.put("DMI", () -> strategyService.runDmiStrategyBacktest(symbol, source, 14, 25, lookback));
        strategies.put("MFI", () -> strategyService.runMfiStrategyBacktest(symbol, source, 14, 80, 20, lookback));
        strategies.put("TrendlineBreakout", () -> strategyService.runTrendlineBreakoutStrategyBacktest(symbol, source, lookback));

        // Moving average crossover strategies
        strategies.put("EMA_Crossover", () -> strategyService.runEmaCrossoverStrategyBacktest(symbol, source, 20, 50, lookback));
        strategies.put("SMA_Crossover", () -> strategyService.runSmaCrossoverStrategyBacktest(symbol, source, 20, 50, lookback));

        // Moving average strategies with different periods
        // strategies.put("SMA10", () -> strategyService.runSimpleSmaStrategyBacktest(symbol, source, 10, lookback));
        // strategies.put("SMA20", () -> strategyService.runSimpleSmaStrategyBacktest(symbol, source, 20, lookback));
        strategies.put("SMA50", () -> strategyService.runSimpleSmaStrategyBacktest(symbol, source, 50, lookback));
        // strategies.put("SMA100", () -> strategyService.runSimpleSmaStrategyBacktest(symbol, source, 100, lookback));
        if (lookback >= 200) strategies.put("SMA200", () -> strategyService.runSimpleSmaStrategyBacktest(symbol, source, 200, lookback));
        // strategies.put("EMA10", () -> strategyService.runSimpleEmaStrategyBacktest(symbol, source, 10, lookback));
        // strategies.put("EMA20", () -> strategyService.runSimpleEmaStrategyBacktest(symbol, source, 20, lookback));
        strategies.put("EMA50", () -> strategyService.runSimpleEmaStrategyBacktest(symbol, source, 50, lookback));
        // strategies.put("EMA100", () -> strategyService.runSimpleEmaStrategyBacktest(symbol, source, 100, lookback));
        if (lookback >= 200) strategies.put("EMA200", () -> strategyService.runSimpleEmaStrategyBacktest(symbol, source, 200, lookback));

        for (Map.Entry<String, Supplier<N_StrategyBacktestDto>> entry : strategies.entrySet()) {
            N_StrategyBacktestDto result = entry.getValue().get();
            List<Object> signalData = new ArrayList<>();
            signalData.add(result.getCurrentSignal());
            signalData.add(result.getWinningTradesRatio());

            decisionMatrixDto.getSignal().put(entry.getKey(), signalData);
        }

        double totalScore = 0.0;
        for (List<Object> signalData : decisionMatrixDto.getSignal().values()) {
            String signal = (String) signalData.get(0);
            double metric = (double) signalData.get(1);

            double score;
            switch (signal) {
                case "STRONG_BUY":
                    score = 1.0;
                    break;
                case "BUY":
                    score = 0.5;
                    break;
                case "HOLD":
                    score = 0.0;
                    break;
                case "SELL":
                    score = -0.5;
                    break;
                case "STRONG_SELL":
                    score = -1.0;
                    break;
                default:
                    score = 0.0;
            }
            totalScore += strategyService.lastDecisionValueGenerator(score, metric);
        }
        double averageScore = totalScore / decisionMatrixDto.getSignal().size();
        String overallDecision = strategyService.lastDecisionValueDescriptor(averageScore);
        decisionMatrixDto.setOverallDecision(overallDecision);

        decisionMatrixDto.getSignalCounts().put("BUY", (int) decisionMatrixDto.getSignal().values().stream().filter(s -> "STRONG_BUY".equals(s.get(0))).count() +
                (int) decisionMatrixDto.getSignal().values().stream().filter(s -> "BUY".equals(s.get(0))).count());

        decisionMatrixDto.getSignalCounts().put("NEUTRAL", (int) decisionMatrixDto.getSignal().values().stream().filter(s -> "NEUTRAL".equals(s.get(0))).count());

        decisionMatrixDto.getSignalCounts().put("SELL", (int) decisionMatrixDto.getSignal().values().stream().filter(s -> "STRONG_SELL".equals(s.get(0))).count() +
                (int) decisionMatrixDto.getSignal().values().stream().filter(s -> "SELL".equals(s.get(0))).count());

        decisionMatrixDto.setSignalCounts(decisionMatrixDto.getSignalCounts());
*/
        return decisionMatrixDto;
    }

}
