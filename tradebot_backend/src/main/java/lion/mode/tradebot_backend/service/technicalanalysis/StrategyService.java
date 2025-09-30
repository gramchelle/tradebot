package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.technicalanalysis.response.StrategyBacktestDto;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.Backtest;
import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;

import com.google.gson.Gson;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.analysis.CashFlow;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.*;
import org.ta4j.core.criteria.pnl.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StockDataRepository stockDataRepository;
    private final BacktestRepository backtestRepository;
    private TradingStatementGenerator generator = new TradingStatementGenerator();
    TradingStatement tradingStatement;
    private Gson gson = new Gson();

    /* ----------------- Indicator-based Strategy Backtest Methods -----------------
     *  Each method runs a backtest for a specific strategy with given parameters  */

    public StrategyBacktestDto runRsiStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int period, int upperLimit, int lowerLimit, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate RSI for period " + period);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("rsiPeriod", period);
        params.put("rsiUpperLimit", upperLimit);
        params.put("rsiLowerLimit", lowerLimit);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        String gsonString = gson.toJson(params);

        Strategy strategy = RuleGeneratorFactory.buildRsiStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto runBollingerBandsStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int period, int stdDevMultiplier, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate Bollinger Bands for period " + period);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("bollingerPeriod", period);
        params.put("bollingerStdDev", stdDevMultiplier);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        Strategy strategy = RuleGeneratorFactory.buildBollingerBandsStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        String gsonString = gson.toJson(params);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto runMacdStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int shortPeriod, int longPeriod, int signalPeriod, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < longPeriod + 1) throw new NotEnoughDataException("Not enough bars to calculate MACD for period " + longPeriod);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("macdShortPeriod", shortPeriod);
        params.put("macdLongPeriod", longPeriod);
        params.put("macdSignalPeriod", signalPeriod);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        String gsonString = gson.toJson(params);

        Strategy strategy = RuleGeneratorFactory.buildMacdStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto runDmiStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int period, int adxThreshold, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate DMI for period " + period);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("dmiPeriod", period);
        params.put("dmiAdxThreshold", adxThreshold);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        String gsonString = gson.toJson(params);

        Strategy strategy = RuleGeneratorFactory.buildDmiStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto runMfiStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int period, int upperLimit, int lowerLimit, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate MFI for period " + period);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("mfiPeriod", period);
        params.put("mfiUpperLimit", upperLimit);
        params.put("mfiLowerLimit", lowerLimit);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        String gsonString = gson.toJson(params);

        Strategy strategy = RuleGeneratorFactory.buildMfiStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto runTrendlineBreakoutStrategyBacktest(String symbol, String source, int surroundingBars, double stopLoss, double takeProfit, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < surroundingBars * 2 + 1) throw new NotEnoughDataException("Not enough bars to calculate Trendline Breakout with surrounding bars " + surroundingBars);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("trendlineSurroundingBars", surroundingBars);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        String gsonString = gson.toJson(params);

        Strategy strategy = RuleGeneratorFactory.buildTrendlineBreakoutStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto runEmaCrossoverStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int shortPeriod, int longPeriod, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < longPeriod + 1) throw new NotEnoughDataException("Not enough bars to calculate EMA Crossover for period " + longPeriod);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("emaCrossoverShortPeriod", shortPeriod);
        params.put("emaCrossoverLongPeriod", longPeriod);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        String gsonString = gson.toJson(params);

        Strategy strategy = RuleGeneratorFactory.buildEMACrossoverStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
   }

    public StrategyBacktestDto runSmaCrossoverStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int shortPeriod, int longPeriod, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < longPeriod + 1) throw new NotEnoughDataException("Not enough bars to calculate SMA Crossover for period " + longPeriod);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("smaCrossoverShortPeriod", shortPeriod);
        params.put("smaCrossoverLongPeriod", longPeriod);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        Strategy strategy = RuleGeneratorFactory.buildSMACrossoverStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        String gsonString = gson.toJson(params);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto runSimpleSmaStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int period, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate SMA for period " + period);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("smaPeriod", period);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        String gsonString = gson.toJson(params);

        Strategy strategy = RuleGeneratorFactory.simpleSmaStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    public StrategyBacktestDto runSimpleEmaStrategyBacktest(String symbol, String source, double stopLoss, double takeProfit, int period, int isTrailingStopLoss, int lookback){
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;
        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate EMA for period " + period);
        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("emaPeriod", period);
        params.put("stopLoss", stopLoss);
        params.put("takeProfit", takeProfit);
        params.put("trailingStopLoss", isTrailingStopLoss);
        params.put("lookback", lookback);

        String gsonString = gson.toJson(params);

        Strategy strategy = RuleGeneratorFactory.simpleEmaStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        StrategyBacktestDto backtestDto = calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
        backtestDto.setParametersJson(gsonString);

        try {
            backtestRepository.save(backtestEntityBuilder(series, backtestDto));
            System.out.println("Backtest report saved for " + symbol + " with strategy " + strategy.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error saving backtest report: " + e.getMessage());
        }
        return backtestDto;
    }

    /* ----------------- Performance Metrics DTO Builder ----------------- */

    // Calculates performance metrics and builds StrategyBacktestDto
    public StrategyBacktestDto calculatePerformanceMetrics(String symbol, Strategy strategy, BarSeries series, TradingRecord tradingRecord, int lookback) {
        String signal = signalGenerator(strategy, series.getEndIndex());
        StrategyBacktestDto dto = new StrategyBacktestDto();
        dto.setSymbol(symbol != null ? symbol : "unknown");
        dto.setStrategyName(strategy != null ? strategy.getName() : "unknown");
        dto.setLookbackPeriod(lookback);
        dto.setLastDecisionValue(scoreGenerator(signal));

        if (series == null || tradingRecord == null) return dto;

        Num totalProfit = new ProfitCriterion().calculate(series, tradingRecord);
        dto.setTotalProfit(totalProfit != null ? totalProfit.doubleValue() : 0.0);
        
        Num returnPercentage = new ReturnCriterion().calculate(series, tradingRecord);
        dto.setGrossReturn(returnPercentage != null ? returnPercentage.doubleValue() : 0.0);
        
        Num averageProfit = new AverageProfitCriterion().calculate(series, tradingRecord);
        dto.setAverageProfit(averageProfit != null ? averageProfit.doubleValue() : 0.0);

        Num rewardRiskRatio = new ReturnOverMaxDrawdownCriterion().calculate(series, tradingRecord);
        dto.setRewardRiskRatio(rewardRiskRatio != null ? rewardRiskRatio.doubleValue()  : 0.0);

        Num maxDrawdown = new MaximumDrawdownCriterion().calculate(series, tradingRecord);
        dto.setMaximumDrawdown(maxDrawdown != null ? maxDrawdown.doubleValue() : 0.0);

        int trades = tradingRecord.getTrades() != null ? tradingRecord.getTrades().size() : 0;
        dto.setTradeCount(trades);

        int posCount = new NumberOfPositionsCriterion().calculate(series, tradingRecord).intValue();
        dto.setPositionCount(posCount);

        tradingStatement = generator.generate(strategy, tradingRecord, series);

        dto.setTotalLoss(tradingStatement.getPerformanceReport().getTotalLoss().doubleValue());
        dto.setTotalProfit(tradingStatement.getPerformanceReport().getTotalProfit().doubleValue());
        dto.setTotalProfitLoss(tradingStatement.getPerformanceReport().getTotalProfitLoss().doubleValue());
        dto.setTotalProfitLossRatioPercent(tradingStatement.getPerformanceReport().getTotalProfitLossPercentage().doubleValue());

        dto.setBreakEvenCount(tradingStatement.getPositionStatsReport().getBreakEvenCount().doubleValue());
        dto.setLastDecisionValue(lastDecisionValueGenerator(dto.getLastDecisionValue(), tradingStatement.getPerformanceReport().getTotalProfitLossPercentage().doubleValue())); //TODO: Metric can be generic
        dto.setLastDecisionValueDescriptor(lastDecisionValueDescriptor(dto.getLastDecisionValue()));

        double winPosRatio = 0.0;
        double lossPosRatio = 0.0;
        try {
            Num winNum = new PositionsRatioCriterion(AnalysisCriterion.PositionFilter.PROFIT).calculate(series, tradingRecord);
            Num lossNum = new PositionsRatioCriterion(AnalysisCriterion.PositionFilter.LOSS).calculate(series, tradingRecord);
            winPosRatio = winNum != null ? winNum.doubleValue() : 0.0;
            lossPosRatio = lossNum != null ? lossNum.doubleValue() : 0.0;
        } catch (Exception ignored) {
        }

        double winningTradesRatio = 0.0;
        try {
            double wins = new NumberOfWinningPositionsCriterion().calculate(series, tradingRecord).doubleValue();
            double losses = new NumberOfLosingPositionsCriterion().calculate(series, tradingRecord).doubleValue();
            double denom = wins + losses;
            winningTradesRatio = denom > 0 ? wins / denom : 0.0;
        } catch (Exception ignored) {
            winningTradesRatio = 0.0;
        }

        // ---------- manual position-level stats, holding periods, equity fallback ----------
        List<Position> positions = tradingRecord.getPositions() != null ? tradingRecord.getPositions() : new ArrayList<>();
        int closedCount = 0;
        int winCount = 0;
        int loseCount = 0;
        double sumProfitPct = 0.0; // sum of pos returns (multiplicative - 1)
        double totalHoldingBars = 0.0;

        int barCount = Math.max(1, series.getBarCount());
        double[] equityFromPositions = new double[barCount];
        for (int i = 0; i < equityFromPositions.length; i++) equityFromPositions[i] = 1.0;
        double capital = 1.0;

        for (Position pos : positions) {
            if (!pos.isClosed()) continue;
            closedCount++;

            Num posProfitNum = null;
            try {
                posProfitNum = pos.getProfit();
            } catch (Exception e) {
                try {
                    Num entryPrice = pos.getEntry().getNetPrice();
                    Num exitPrice = pos.getExit().getNetPrice();
                    posProfitNum = (entryPrice != null && exitPrice != null)
                            ? exitPrice.dividedBy(entryPrice)
                            : series.numFactory().numOf(1.0);
                } catch (Exception ex) {
                    posProfitNum = series.numFactory().numOf(1.0);
                }
            }

            double posProfitMul = posProfitNum != null ? posProfitNum.doubleValue() : 1.0;
            double posProfitPct = posProfitMul - 1.0;
            sumProfitPct += posProfitPct;
            if (posProfitPct > 0) winCount++;
            else if (posProfitPct < 0) loseCount++;

            int entryIndex = Math.max(0, pos.getEntry().getIndex());
            int exitIndex = Math.max(0, pos.getExit().getIndex());
            int holdingBars = Math.max(1, exitIndex - entryIndex + 1);
            totalHoldingBars += holdingBars;

            // apply multiplicative update at exit bar (simple model)
            capital = capital * posProfitMul;
            if (exitIndex >= 0 && exitIndex < equityFromPositions.length) {
                equityFromPositions[exitIndex] = capital;
            }
        }

        // fill-forward equityFromPositions
        for (int i = 1; i < equityFromPositions.length; i++) {
            if (equityFromPositions[i] == 1.0 && equityFromPositions[i - 1] != 1.0) equityFromPositions[i] = equityFromPositions[i - 1];
            else if (equityFromPositions[i] == 0.0) equityFromPositions[i] = equityFromPositions[i - 1];
        }

        // finalize holding metrics
        dto.setTotalHoldingPeriod(totalHoldingBars);
        dto.setAverageHoldingPeriod(closedCount > 0 ? totalHoldingBars / closedCount : 0.0);

        // finalize win/loss ratios (fallback to manual if Criteria failed)
        if (closedCount > 0) {
            if (winPosRatio == 0.0 && lossPosRatio == 0.0) {
                int total = winCount + loseCount;
                if (total > 0) {
                    winPosRatio = (double) winCount / total;
                    lossPosRatio = (double) loseCount / total;
                } else {
                    winPosRatio = 0.0; lossPosRatio = 0.0;
                }
            }
        } else {
            winPosRatio = 0.0;
            lossPosRatio = 0.0;
        }
        dto.setWinningPositionsRatio(winPosRatio);
        dto.setLosingPositionsRatio(lossPosRatio);

        // ---------- Buy & Hold ----------
        double buyAndHoldReturn = 0.0;
        try {
            Num firstClose = series.getBar(0).getClosePrice();
            Num lastClose = series.getLastBar().getClosePrice();
            if (firstClose != null && lastClose != null && firstClose.doubleValue() > 0) {
                buyAndHoldReturn = lastClose.dividedBy(firstClose).doubleValue() - 1.0;
            }
        } catch (Exception ignored) {
            buyAndHoldReturn = 0.0;
        }
        dto.setBuyAndHoldReturn(buyAndHoldReturn);

        try {
            // grossReturn already set above via ReturnCriterion; if 0 try fallback final capital -1
            if (dto.getGrossReturn() == 0.0) {
                // attempt fallback from CashFlow final value
                try {
                    CashFlow cf = new CashFlow(series, tradingRecord);
                    Num last = cf.getValue(series.getEndIndex());
                    if (last != null) dto.setGrossReturn(last.doubleValue() - 1.0);
                } catch (Exception ignored) { /* ignore */ }
            }
        } catch (Exception ignored) { /* ignore */ }

        dto.setVersusBuyAndHold(dto.getGrossReturn() - dto.getBuyAndHoldReturn());

        // ---------- CashFlow (final equity) & profitLoss ----------
        double finalCash = 0.0;
        try {
            CashFlow cf = new CashFlow(series, tradingRecord);
            Num lastVal = cf.getValue(series.getEndIndex());
            finalCash = lastVal != null ? lastVal.doubleValue() : 0.0;
            dto.setCashFlow(finalCash);
        } catch (Exception ignored) {
            // fallback: use multiplicative capital from positions
            finalCash = capital;
            dto.setCashFlow(finalCash);
        }
        // profitLoss: final - initial(=1)
        dto.setProfitLoss(finalCash > 0 ? finalCash - 1.0 : 0.0);

        // ---------- Sharpe & Sortino (use cashflow series if available, else equityFromPositions) ----------
        List<Double> perBarReturns = new ArrayList<>();
        try {
            CashFlow cf = new CashFlow(series, tradingRecord);
            boolean cfUsable = true;
            for (int i = 0; i <= series.getEndIndex(); i++) {
                Num v = null;
                try { v = cf.getValue(i); } catch (Exception ex) { v = null; }
                if (v == null) { cfUsable = false; break; }
            }
            if (cfUsable) {
                // build per-bar returns from cf
                for (int i = 1; i <= series.getEndIndex(); i++) {
                    Num prev = cf.getValue(i - 1);
                    Num cur = cf.getValue(i);
                    double prevD = prev != null ? prev.doubleValue() : 0.0;
                    double curD = cur != null ? cur.doubleValue() : prevD;
                    if (prevD <= 0) perBarReturns.add(0.0);
                    else perBarReturns.add((curD / prevD) - 1.0);
                }
            } else {
                // fallback to equityFromPositions
                for (int i = 1; i < equityFromPositions.length; i++) {
                    double prev = equityFromPositions[i - 1];
                    double cur = equityFromPositions[i];
                    if (prev <= 0) perBarReturns.add(0.0);
                    else perBarReturns.add((cur / prev) - 1.0);
                }
            }
        } catch (Exception ignored) {
            // fallback: equityFromPositions
            for (int i = 1; i < equityFromPositions.length; i++) {
                double prev = equityFromPositions[i - 1];
                double cur = equityFromPositions[i];
                if (prev <= 0) perBarReturns.add(0.0);
                else perBarReturns.add((cur / prev) - 1.0);
            }
        }

        double mean = mean(perBarReturns);
        double std = std(perBarReturns);
        final double ANNUAL_FACTOR = Math.sqrt(252.0);
        double sharpe = std > 0 ? (mean / std) * ANNUAL_FACTOR : 0.0;
        dto.setSharpeRatio(sharpe);

        double downsideStd = downsideStd(perBarReturns);
        double sortino = downsideStd > 0 ? (mean / downsideStd) * ANNUAL_FACTOR : 0.0;
        dto.setSortinoRatio(sortino);

        return dto;
    }

    // Creates Backtest entity from DTO for saving to DB
    public Backtest backtestEntityBuilder(BarSeries series, StrategyBacktestDto backtestDto){
        Backtest backtestReport = new Backtest();
        backtestReport.setSymbol(backtestDto.getSymbol());
        backtestReport.setStrategyName(backtestDto.getStrategyName());
        backtestReport.setTimeInterval("1d");
        backtestReport.setLookbackPeriod(backtestDto.getLookbackPeriod());  
        backtestReport.setLastSignal(backtestDto.getLastDecisionValueDescriptor());
        backtestReport.setScore(backtestDto.getLastDecisionValue());
        backtestReport.setTotalTrades(backtestDto.getTradeCount());
        backtestReport.setTotalProfit(backtestDto.getTotalProfit());
        backtestReport.setTotalLoss(backtestDto.getTotalLoss());
        backtestReport.setTotalProfitLossRatioPercent(backtestDto.getTotalProfitLossRatioPercent());
        backtestReport.setRewardRiskRatio(backtestDto.getRewardRiskRatio());
        backtestReport.setSharpeRatio(backtestDto.getSharpeRatio());
        backtestReport.setSortinoRatio(backtestDto.getSortinoRatio());

        int lookback = backtestDto.getLookbackPeriod();
        int endIndex = series.getEndIndex();
        int startIndex = Math.max(0, endIndex - lookback);

        backtestReport.setBacktestEndDate(series.getBar(endIndex).getEndTime());
        backtestReport.setBacktestStartDate(series.getBar(startIndex).getEndTime());

        backtestReport.setParametersJson(backtestDto.getParametersJson());

        return backtestReport;
    }

    /* ----------------- Helper Stats Methods ----------------- */

    private static double mean(List<Double> arr) {
        if (arr == null || arr.isEmpty()) return 0.0;
        double s = 0.0;
        for (double v : arr) s += v;
        return s / arr.size();
    }

    private static double std(List<Double> arr) {
        if (arr == null || arr.size() < 2) return 0.0;
        double m = mean(arr);
        double s = 0.0;
        for (double v : arr) s += (v - m) * (v - m);
        return Math.sqrt(s / (arr.size() - 1));
    }

    private static double downsideStd(List<Double> arr) {
        if (arr == null || arr.isEmpty()) return 0.0;
        List<Double> downs = new ArrayList<>();
        for (double v : arr) if (v < 0) downs.add(v);
        if (downs.isEmpty()) return 0.0;
        if (downs.size() == 1) return Math.abs(downs.get(0));
        double m = mean(downs);
        double s = 0.0;
        for (double v : downs) s += (v - m) * (v - m);
        return Math.sqrt(s / (downs.size() - 1));
    }

    /* --------- Data Manipulation Helper Methods ----------------- */

    // Generates a trading signal based on the strategy's entry and exit rules
    public String signalGenerator(Strategy strategy, int targetIndex) {
        if (strategy.shouldEnter(targetIndex)) {
            return "BUY";
        } else if (strategy.shouldExit(targetIndex)) {
            return "SELL";
        }
        return "NEUTRAL";
    }

    // Converts signal descriptors to numerical scores
    public double scoreGenerator(String signal){
        switch (signal) {
            case "BUY": case "STRONG_BUY":
                return 1.0;
            case "SELL": case "STRONG_SELL":
                return -1.0;
            default:
                return 0.0;
        }
    }

    // Combines score and a performance metric into a last decision value
    private double lastDecisionValueGenerator(double score, double metric){
        double normalizedMetric = Math.tanh(metric); // -1 ile 1 arasında sınırlar
        return score * normalizedMetric;
    }

    // Maps the last decision value to a signal descriptor
    public String lastDecisionValueDescriptor(double value){
        if (value > 0.2 && value <= 1) return "STRONG_BUY";
        else if (value > 0.05) return "BUY";
        else if (value > -0.05 && value < 0.05) return "NEUTRAL";
        else if (value < -0.05) return "SELL";
        else if (value < -0.2 && value >= -1) return "STRONG_SELL";
        else return "WRONG_SIGNAL";
    }

    // Loads price data for a specific symbol into a BarSeries
    public BarSeries loadSeries(String symbol) {
        symbol = symbol.toUpperCase();
        if (stockDataRepository.findTopBySymbolOrderByTimestampDesc(symbol).isEmpty()) throw new NotEnoughDataException("No data found for symbol: " + symbol);

        List<StockDataDaily> dataList = stockDataRepository.findBySymbolOrderByTimestampAsc(symbol);
        BarSeries series = new BaseBarSeriesBuilder()
                .withName(symbol)
                .build();


        for (StockDataDaily data : dataList) {
            LocalDateTime localEndTime = data.getTimestamp();
            ZonedDateTime zonedEndTime = localEndTime.atZone(ZoneId.systemDefault());
            Instant endTimeInstant = zonedEndTime.toInstant();

            if (series.getBarCount() > 0) {
                Instant lastEnd = series.getBar(series.getEndIndex()).getEndTime();
                if (!endTimeInstant.isAfter(lastEnd)) {
                    continue;
                }
            }

            Bar bar = new BaseBar(
                    Duration.ofDays(1),
                    endTimeInstant,
                    DecimalNum.valueOf(data.getOpen()),
                    DecimalNum.valueOf(data.getHigh()),
                    DecimalNum.valueOf(data.getLow()),
                    DecimalNum.valueOf(data.getClose()),
                    DecimalNum.valueOf(data.getVolume()),
                    DecimalNum.valueOf(0),
                    0
            );

            series.addBar(bar);
        }

        return series;
    }

    // Selects the appropriate price indicator based on the price type
    public Indicator<Num> sourceSelector(String priceType, BarSeries series) {
        switch (priceType.toLowerCase()) {
            case "open":
                return new OpenPriceIndicator(series);
            case "close":
                return new ClosePriceIndicator(series);
            case "high":
                return new HighPriceIndicator(series);
            case "low":
                return new LowPriceIndicator(series);
            default:
                throw new IllegalArgumentException("Invalid price type: " + priceType);
        }
    }

    // Slices the BarSeries to include only the last 'lookbackBars' bars
    public BarSeries sliceSeriesByLookback(BarSeries originalSeries, int lookbackBars) {
        int total = originalSeries.getBarCount();
        if (lookbackBars >= total) {
            return originalSeries;
        }
        int startIndex = Math.max(0, total - lookbackBars);
        BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder().withName(originalSeries.getName() + "_lb" + lookbackBars);
        BarSeries sliced = builder.build();

        for (int i = startIndex; i < total; i++) {
            sliced.addBar(originalSeries.getBar(i));
        }
        return sliced;
    }

}