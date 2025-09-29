package lion.mode.tradebot_backend.service.technicalanalysis;

import java.time.Instant;
import java.util.*;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.MaximumDrawdownCriterion;
import org.ta4j.core.criteria.ReturnOverMaxDrawdownCriterion;
import org.ta4j.core.criteria.pnl.AverageProfitCriterion;
import org.ta4j.core.criteria.pnl.ProfitCriterion;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;
import org.ta4j.core.rules.BooleanRule;

import com.google.gson.Gson;

import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto;
import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto.IndicatorParam;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.MovingAveragesOverallResponse;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.StrategyBacktestDto;
import lion.mode.tradebot_backend.model.WalkForwardReport;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.repository.WalkForwardReportRepository;
import lombok.RequiredArgsConstructor;
import ta4jexamples.strategies.CCICorrectionStrategy;

@Service
@RequiredArgsConstructor
public class WalkForwardOptimizationService {

    private final WalkForwardReportRepository walkForwardReportRepository;
    private final StrategyService strategyService;
    private TradingStatementGenerator generator = new TradingStatementGenerator();

    public List<WalkForwardReport> runDetailedWalkForwardAnalysis(WalkForwardRequestDto request) {
        String symbol = request.getSymbol().toUpperCase();
        BarSeries fullSeries = strategyService.loadSeries(symbol);

        if (fullSeries == null || fullSeries.isEmpty()) throw new IllegalStateException("No data for symbol: " + symbol);
        WalkForwardReport valReport = null;
        List<WalkForwardReport> reports = new ArrayList<>();

        int optWindow = request.getOptimizationWindow();
        int valWindow = request.getValidationWindow();
        int rollStep = request.getRollStep();

        double trainTotalPnL = 0.0, valTotalPnL = 0.0;
        int trainTradeSignals = 0, valTradeSignals = 0;
        double totalTrainPnLperTrade = 0.0;
        double totalValPnLperTrade = 0.0;

        System.out.println("\n[INFO] Starting walk-forward analysis for symbol: " + symbol);
        // Walk-forward loop
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

            // Optimization (grid search)
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

            // Validation test
            Strategy valStrategy = strategySelector(bestTrainStrategy.getName(), valSlice, new ClosePriceIndicator(valSlice), bestParams);
            TradingRecord valRecord = new BarSeriesManager(valSlice).run(valStrategy);

            WalkForwardReport trainReport = buildReport("TRAIN", trainSlice, bestTrainStrategy, bestTrainRecord, bestParams, optWindow, valWindow, request.getStep());
            valReport = buildReport("VALIDATION", valSlice, valStrategy, valRecord, bestParams, optWindow, valWindow, request.getStep());
            
            trainTotalPnL += trainReport.getTotalProfit();
            trainTradeSignals += trainReport.getNumberOfTrades();
            valTotalPnL += valReport.getTotalProfit();
            valTradeSignals += valReport.getNumberOfTrades();

            reports.add(trainReport);
            reports.add(valReport);
            trainSlice = null;
            valSlice = null;
        }

        try {
            walkForwardReportRepository.save(valReport);
            System.out.println("Walk-forward analysis completed for symbol: " + symbol);
        } catch (Exception e) {
            System.err.println("Error saving walk-forward report: " + e.getMessage());
        }

        totalTrainPnLperTrade = (trainTradeSignals != 0) ? (trainTotalPnL / trainTradeSignals) : 0.0;
        totalValPnLperTrade = (valTradeSignals != 0) ? (valTotalPnL / valTradeSignals) : 0.0;
        double confidence = (totalValPnLperTrade != 0) ? (totalTrainPnLperTrade / totalValPnLperTrade) * 100 : 0.0;
        System.out.println("Confidence level: " + String.format("%.2f", confidence) + "% (Train PnL/Trade: " + String.format("%.4f", totalTrainPnLperTrade) + ", Val PnL/Trade: " + String.format("%.4f", totalValPnLperTrade) + ")");
        valReport.setConfidence(confidence);
        
        return reports;
    }

    public WalkForwardReport runWalkForwardAnalysis(WalkForwardRequestDto request) {
        String symbol = request.getSymbol().toUpperCase();
        BarSeries fullSeries = strategyService.loadSeries(symbol);

        if (fullSeries == null || fullSeries.isEmpty()) throw new IllegalStateException("No data for symbol: " + symbol);
        WalkForwardReport valReport = null;
        List<WalkForwardReport> reports = new ArrayList<>();

        int optWindow = request.getOptimizationWindow();
        int valWindow = request.getValidationWindow();
        int rollStep = request.getRollStep();

        double trainTotalPnL = 0.0, valTotalPnL = 0.0;
        int trainTradeSignals = 0, valTradeSignals = 0;
        double totalTrainPnLperTrade = 0.0;
        double totalValPnLperTrade = 0.0;

        System.out.println("\n[INFO] Starting walk-forward analysis for symbol: " + symbol);
        // Walk-forward loop
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

            // Optimization (grid search)
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

                    double score = generator.generate(candidate, record, fullSeries).getPerformanceReport().getTotalProfitLoss().doubleValue();
                    if (score > bestScore) {
                        bestScore = score;
                        bestTrainStrategy = candidate;
                        bestTrainRecord = record;
                        bestParams = candidateParams;
                    }
                }
            }

            if (bestTrainStrategy == null) continue;

            // Validation test
            Strategy valStrategy = strategySelector(bestTrainStrategy.getName(), valSlice, new ClosePriceIndicator(valSlice), bestParams);
            TradingRecord valRecord = new BarSeriesManager(valSlice).run(valStrategy);

            WalkForwardReport trainReport = buildReport("TRAIN", trainSlice, bestTrainStrategy, bestTrainRecord, bestParams, optWindow, valWindow, request.getStep());
            valReport = buildReport("VALIDATION", valSlice, valStrategy, valRecord, bestParams, optWindow, valWindow, request.getStep());

            trainTotalPnL += trainReport.getTotalProfit();
            trainTradeSignals += trainReport.getNumberOfTrades();
            valTotalPnL += valReport.getTotalProfit();
            valTradeSignals += valReport.getNumberOfTrades();

            reports.add(valReport);
            trainSlice = null;
            valSlice = null;
        }

        try {
            walkForwardReportRepository.save(valReport);
            System.out.println("Walk-forward analysis completed for symbol: " + symbol);
        } catch (Exception e) {
            System.err.println("Error saving walk-forward report: " + e.getMessage());
        }

        totalTrainPnLperTrade = (trainTradeSignals != 0) ? (trainTotalPnL / trainTradeSignals) : 0.0;
        totalValPnLperTrade = (valTradeSignals != 0) ? (valTotalPnL / valTradeSignals) : 0.0;
        double confidence = (totalValPnLperTrade != 0) ? (totalTrainPnLperTrade / totalValPnLperTrade) * 100 : 0.0;
        System.out.println("Confidence level: " + String.format("%.2f", confidence) + "% (Train PnL/Trade: " + String.format("%.4f", totalTrainPnLperTrade) + ", Val PnL/Trade: " + String.format("%.4f", totalValPnLperTrade) + ")");
        valReport.setConfidence(confidence);
        
        return valReport;
    }

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

    private WalkForwardReport buildReport(String phase, BarSeries slice, Strategy strategy, TradingRecord record, Map<String, Object> params, int optWindow, int valWindow, int step) {
        WalkForwardReport report = new WalkForwardReport();
        report.setStrategyName(strategy.getName() + " (" + phase + ")");
        report.setSymbol(slice.getName());
        report.setRunDate(Instant.now());
        report.setStartDate(slice.getFirstBar().getEndTime());
        report.setEndDate(slice.getLastBar().getEndTime());

        report.setOptimizationWindow(optWindow);
        report.setValidationWindow(valWindow);
        report.setStepSize(step);

        report.setParameters(params != null ? params.toString() : "{}");

        if (record != null) {
            report.setNumberOfTrades(record.getTrades().size());
            report.setNumberOfPositions(record.getPositions().size());

            StrategyBacktestDto backtestDto = strategyService.calculatePerformanceMetrics(slice.getName(), strategy, slice, record, step);
            report.setTotalProfitLossRatio(backtestDto.getTotalProfitLoss());
            report.setTotalProfitLossRatioPercent(backtestDto.getTotalProfitLossRatioPercent());
            report.setLastSignal(backtestDto.getCurrentSignal());
            report.setLastSignalDate(slice.getLastBar().getEndTime());
            report.setLastPrice(slice.getLastBar().getClosePrice().doubleValue());
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
