package lion.mode.tradebot_backend.service.technicalanalysis;

import java.time.Instant;
import java.util.*;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.TradingStatementGenerator;
import org.ta4j.core.rules.BooleanRule;

import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto;
import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto.IndicatorParam;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.MovingAveragesOverallResponse;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.StrategyBacktestDto;
import lion.mode.tradebot_backend.model.WalkForwardReport;
import lion.mode.tradebot_backend.repository.WalkForwardReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalkForwardOptimizationService {

    private final WalkForwardReportRepository walkForwardReportRepository;
    private final StrategyService strategyService;
    private TradingStatementGenerator generator = new TradingStatementGenerator();

    public List<WalkForwardReport> runDetailedWalkForwardAnalysis(WalkForwardRequestDto request) {
        return coreWalkForward(request, true);
    }

    public WalkForwardReport runWalkForwardAnalysis(WalkForwardRequestDto request) {
        List<WalkForwardReport> reports = coreWalkForward(request, false);
        return reports.isEmpty() ? null : reports.get(0);
    }

    private List<WalkForwardReport> coreWalkForward(WalkForwardRequestDto request, boolean returnAllReports) {
        String symbol = request.getSymbol().toUpperCase();
        BarSeries fullSeries = strategyService.loadSeries(symbol);

        List<WalkForwardReport> reports = new ArrayList<>();
        WalkForwardReport valReport = null;

        int optWindow = request.getOptimizationWindow();
        int valWindow = request.getValidationWindow();
        int rollStep = request.getRollStep();

        double trainTotalPnL = 0.0, valTotalPnL = 0.0;
        int trainTradeSignals = 0, valTradeSignals = 0;

        System.out.println("\n[INFO] Starting walk-forward analysis for symbol: " + symbol);

        for (int start = 0; start + optWindow + valWindow <= fullSeries.getBarCount(); start += rollStep) {
            int trainStart = start;
            int trainEnd = start + optWindow;
            int valEnd = trainEnd + valWindow;

            BarSeries trainSlice = slice(fullSeries, trainStart, trainEnd);
            BarSeries valSlice = slice(fullSeries, trainEnd, valEnd);

            double bestScore = Double.NEGATIVE_INFINITY;
            Strategy bestTrainStrategy = null;
            TradingRecord bestTrainRecord = null;
            Map<String, Object> bestParams = null;

            // Optimization
            for (IndicatorParam indicatorParam : request.getIndicators()) {
                List<Map<String, Object>> paramCombos = generateParamCombinations(indicatorParam.getParams());
                for (Map<String, Object> candidateParams : paramCombos) {
                    Strategy candidate = strategySelector(
                            indicatorParam.getType(),
                            trainSlice,
                            new ClosePriceIndicator(trainSlice),
                            candidateParams
                    );
                    TradingRecord record = new BarSeriesManager(trainSlice).run(candidate);
                    double score = generator.generate(candidate, record, trainSlice).getPerformanceReport().getTotalProfitLoss().doubleValue();

                    if (score > bestScore) {
                        bestScore = score;
                        bestTrainStrategy = candidate;
                        bestTrainRecord = record;
                        bestParams = candidateParams;
                    }
                }
            }

            if (bestTrainStrategy == null) continue;

            // Validation
            Strategy valStrategy = strategySelector(bestTrainStrategy.getName(), valSlice, new ClosePriceIndicator(valSlice), bestParams);
            TradingRecord valRecord = new BarSeriesManager(valSlice).run(valStrategy);

            WalkForwardReport trainReport = buildReport("TRAIN", trainSlice, fullSeries, bestTrainStrategy, bestTrainRecord, bestParams, optWindow, valWindow, rollStep);
            valReport = buildReport("VALIDATION", valSlice, fullSeries, valStrategy, valRecord, bestParams, optWindow, valWindow, rollStep);

            trainTotalPnL += trainReport.getTotalProfit();
            trainTradeSignals += trainReport.getNumberOfTrades();
            valTotalPnL += valReport.getTotalProfit();
            valTradeSignals += valReport.getNumberOfTrades();

            if (returnAllReports) {
                reports.add(trainReport);
                reports.add(valReport);
            }

            trainSlice = null;
            valSlice = null;
        }

        // Confidence calculation
        double totalTrainPnLperTrade = (trainTradeSignals != 0) ? (trainTotalPnL / trainTradeSignals) : 0.0;
        double totalValPnLperTrade = (valTradeSignals != 0) ? (valTotalPnL / valTradeSignals) : 0.0;
        double confidence = (totalValPnLperTrade != 0) ? (totalTrainPnLperTrade / totalValPnLperTrade) * 100 : 0.0;

        if (valReport != null) valReport.setConfidence(confidence);

        try {
            walkForwardReportRepository.save(valReport);
            System.out.println("Walk-forward analysis completed for symbol: " + symbol);
        } catch (Exception e) {
            System.err.println("Error saving walk-forward report: " + e.getMessage());
        }

        if (returnAllReports) {
            return reports;
        } else {
            return valReport != null ? List.of(valReport) : List.of();
        }
    }

    // Generate all combinations of parameters for grid search
    private List<Map<String, Object>> generateParamCombinations(Map<String, Object> rawParams) {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(new HashMap<>());

        for (Map.Entry<String, Object> entry : rawParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            List<Object> values;
            if (value instanceof Map) {
                Map<String, Object> vmap = (Map<String, Object>) value;
                int min = (int) vmap.getOrDefault("min", 0);
                int max = (int) vmap.getOrDefault("max", 0);
                int step = (int) vmap.getOrDefault("step", 1);
                values = new ArrayList<>();
                for (int i = min; i <= max; i += step) values.add(i);
            } else {
                values = List.of(value); // sabit parametre
            }

            List<Map<String, Object>> newResults = new ArrayList<>();
            for (Map<String, Object> base : results) {
                for (Object v : values) {
                    Map<String, Object> copy = new HashMap<>(base);
                    copy.put(key, v);
                    newResults.add(copy);
                }
            }
            results = newResults;
        }
        return results;
    }

    // Report builder for database storage
    private WalkForwardReport buildReport(String phase, BarSeries slice, BarSeries fullSeries, Strategy strategy, TradingRecord record, Map<String, Object> params, int optWindow, int valWindow, int rollStep) {
        WalkForwardReport report = new WalkForwardReport();
        report.setStrategyName(strategy.getName() + " (" + phase + ")");
        report.setSymbol(slice.getName());
        report.setRunDate(Instant.now());
        report.setStartDate(slice.getFirstBar().getEndTime());
        report.setEndDate(slice.getLastBar().getEndTime());

        report.setOptimizationWindow(optWindow);
        report.setValidationWindow(valWindow);
        report.setStepSize(rollStep);

        report.setParameters(params != null ? params.toString() : "{}");

        if (record != null) {

            report.setNumberOfTrades(record.getTrades().size());
            report.setNumberOfPositions(record.getPositions().size());

            StrategyBacktestDto backtestDto = strategyService.calculatePerformanceMetrics(slice.getName(), strategy, slice, record, rollStep);
            report.setTotalProfitLossRatio(backtestDto.getTotalProfitLoss());
            report.setTotalProfitLossRatioPercent(backtestDto.getTotalProfitLossRatioPercent());

            report.setLastSignal(backtestDto.getLastDecisionValueDescriptor());
            report.setLastSignalDate(fullSeries.getLastBar().getEndTime());
            report.setLastPrice(fullSeries.getLastBar().getClosePrice().doubleValue());

            report.setTotalProfit(backtestDto.getTotalProfit());
            report.setGrossReturn(backtestDto.getGrossReturn());
            report.setAverageProfit(backtestDto.getAverageProfit());
            report.setReturnOverMaxDrawdown(backtestDto.getRewardRiskRatio());
            report.setTotalLoss(backtestDto.getTotalLoss());
        }
        
        return report;
    }

    private BarSeries slice(BarSeries full, int start, int end) {
        return new BaseBarSeriesBuilder()
                .withName(full.getName() + "_bars_between_" + start + "_-_" + end)
                .withBars(full.getBarData().subList(start, end))
                .build();
    }

    public static Strategy strategySelector(String type, BarSeries series, Indicator<Num> closePrice, Map<String, Object> params) {
        String normalized = type.trim().toLowerCase(Locale.ENGLISH)
                .replace("-", "_")
                .replace(" ", "_")
                .replace("_only", "")
                .replace("-", "")
                .replace("-only", "")
                .replace("_strategy", "")
                .replace(",", "");

        if (normalized.contains("and")) {
            String[] parts = normalized.split("_and_");
            Rule entry = null;
            Rule exit = null;

            for (String part : parts) {
                Strategy s = strategySelector(part, series, closePrice, params);
                entry = (entry == null) ? s.getEntryRule() : entry.or(s.getEntryRule());
                exit = (exit == null) ? s.getExitRule() : exit.or(s.getExitRule());
            }
            return new BaseStrategy(type.replace("i", "I").toUpperCase(), entry, exit);
        }
        
        //System.out.println("[NORMALIZED]: " + normalized + " for type: " + type);
        switch (normalized) {
            case "rsi": return RuleGeneratorFactory.buildRsiStrategy(series, closePrice, params);
            case "macd": return RuleGeneratorFactory.buildMacdStrategy(series, closePrice, params);
            // case "ema": return RuleGeneratorFactory.simpleEmaStrategy(series, closePrice, params);
            // case "sma": return RuleGeneratorFactory.simpleSmaStrategy(series, closePrice, params);
            case "ema10": return RuleGeneratorFactory.buildFixedEmaStrategy(series, closePrice, 10, params);
            case "ema20": return RuleGeneratorFactory.buildFixedEmaStrategy(series, closePrice, 20, params);
            case "ema30": return RuleGeneratorFactory.buildFixedEmaStrategy(series, closePrice, 30, params);
            case "ema50": return RuleGeneratorFactory.buildFixedEmaStrategy(series, closePrice, 50, params);
            case "ema100": return RuleGeneratorFactory.buildFixedEmaStrategy(series, closePrice, 100, params);
            case "ema200": return RuleGeneratorFactory.buildFixedEmaStrategy(series, closePrice, 200, params);
            case "sma10": return RuleGeneratorFactory.buildFixedSmaStrategy(series, closePrice, 10, params);
            case "sma20": return RuleGeneratorFactory.buildFixedSmaStrategy(series, closePrice, 20, params);
            case "sma30": return RuleGeneratorFactory.buildFixedSmaStrategy(series, closePrice, 30, params);
            case "sma50": return RuleGeneratorFactory.buildFixedSmaStrategy(series, closePrice, 50, params);
            case "sma100": return RuleGeneratorFactory.buildFixedSmaStrategy(series, closePrice, 100, params);
            case "sma200": return RuleGeneratorFactory.buildFixedSmaStrategy(series, closePrice, 200, params);
            case "ema_crossover": case "ema_cross": return RuleGeneratorFactory.buildEMACrossoverStrategy(series, closePrice, params);
            case "sma_crossover": case "sma_cross": return RuleGeneratorFactory.buildSMACrossoverStrategy(series, closePrice, params);
            //case "cci": return CCICorrectionStrategy.buildStrategy(series);
            case "mfi": return RuleGeneratorFactory.buildMfiStrategy(series, closePrice, params);
            case "dmi": return RuleGeneratorFactory.buildDmiStrategy(series, closePrice, params);
            case "bollinger": return RuleGeneratorFactory.buildBollingerBandsStrategy(series, closePrice, params);
            case "trendline": return RuleGeneratorFactory.buildTrendlineBreakoutStrategy(series, closePrice, params);
            default: throw new IllegalArgumentException("Unknown strategy type: " + type);
        }
    }

    /// HELPERS
    
    public MovingAveragesOverallResponse getMovingAverages(String symbol) {
        BarSeries series = strategyService.loadSeries(symbol.toUpperCase());
        if (series == null || series.isEmpty()) 
            throw new IllegalStateException("No data for symbol: " + symbol);

        MovingAveragesOverallResponse response = new MovingAveragesOverallResponse();

        int[] periods = {10, 20, 30, 50, 100, 200};
        boolean[] types = {false, true};

        List<Rule> buyRules = new ArrayList<>();
        List<Rule> sellRules = new ArrayList<>();
        List<Rule> neutralRules = new ArrayList<>();

        for (boolean isEma : types) {
            for (int period : periods) {
                var strategy = maStrategyGenerator(period, isEma, series);

                Rule buyRule = new BooleanRule(strategy.shouldEnter(series.getEndIndex()));
                Rule sellRule = new BooleanRule(strategy.shouldExit(series.getEndIndex()));

                buyRules.add(buyRule);
                sellRules.add(sellRule);
            }
        }

        Rule combinedBuyRule = buyRules.stream().reduce(Rule::or).orElseThrow(() -> new IllegalStateException("No buy rules found"));
        Rule combinedSellRule = sellRules.stream().reduce(Rule::or).orElseThrow(() -> new IllegalStateException("No sell rules found"));

        String overallSignal;
        int buySignals = combinedBuyRule.isSatisfied(series.getEndIndex()) ? 1 : 0;
        int sellSignals = combinedSellRule.isSatisfied(series.getEndIndex()) ? 1 : 0;
        int neutralSignals = (buySignals == 0 && sellSignals == 0) ? 1 : 0;

        response.getSignals().put("Buy", buySignals);
        response.getSignals().put("Sell", sellSignals);
        response.getSignals().put("Neutral", neutralSignals);

        overallSignal = (buySignals > sellSignals) ? "Buy" :
                        (sellSignals > buySignals) ? "Sell" : "Neutral";


        response.setOverallSignal(overallSignal);
        for (boolean isEma : types) {
            String prefix = isEma ? "EMA" : "SMA";
            for (int period : periods) {
                var strategy = maStrategyGenerator(period, isEma, series);
                String signal = signalGenerator(strategy.shouldEnter(series.getEndIndex()), 
                                                strategy.shouldExit(series.getEndIndex()));
                response.getMovingAverages().put(prefix + period, signal);
            }
        }

        return response;
    }

    private String signalGenerator(boolean shouldEnter, boolean shouldExit){
        if (shouldEnter) return "Buy";
        else if (shouldExit) return "Sell";
        else return "Neutral";
    }

    private Strategy maStrategyGenerator(int period, boolean isEma, BarSeries series) {
        Indicator<Num> closePrice = new ClosePriceIndicator(series);
        Map<String, Object> params = new HashMap<>();
        if (isEma) {
            return RuleGeneratorFactory.buildFixedEmaStrategy(series, closePrice, period, params);
        } else {
            return RuleGeneratorFactory.buildFixedSmaStrategy(series, closePrice, period, params);
        }
    }

}
