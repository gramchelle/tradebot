package lion.mode.tradebot_backend.service.N_technicalanalysis;

import lion.mode.tradebot_backend.dto.base_responses.N_StrategyBacktestDto;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
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
import org.ta4j.core.reports.PerformanceReport;
import org.ta4j.core.reports.PositionStatsReport;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class N_StrategyService {

    private final StockDataRepository repository;
    private TradingStatementGenerator generator = new TradingStatementGenerator();


    // TODO: Save strategy templates to DB -> user can select from saved strategies
    // TODO: Add custom strategy backtest endpoints
    // TODO: Add grid search for optimal params
    // TODO: Add walk-forward optimization
    // TODO: Add Monte Carlo simulations
    // TODO: Add caching for loaded series

    /// ----------------- Indicator-based Strategy Backtest Methods -----------------

    public N_StrategyBacktestDto runRsiStrategyBacktest(String symbol, String source, int period, int upperLimit, int lowerLimit, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate RSI for period " + period);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("period", period);
        params.put("upperLimit", upperLimit);
        params.put("lowerLimit", lowerLimit);

        Strategy strategy = RuleGeneratorFactory.buildRsiStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runBollingerBandsStrategyBacktest(String symbol, String source, int period, int stdDevMultiplier, String basisMaType, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate Bollinger Bands for period " + period);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("period", period);
        params.put("stdDevMultiplier", stdDevMultiplier);
        params.put("basisMaType", basisMaType);

        Strategy strategy = RuleGeneratorFactory.buildBollingerBandsStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runMacdStrategyBacktest(String symbol, String source, int shortPeriod, int longPeriod, int signalPeriod, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < longPeriod + 1) throw new NotEnoughDataException("Not enough bars to calculate MACD for period " + longPeriod);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("shortPeriod", shortPeriod);
        params.put("longPeriod", longPeriod);
        params.put("signalPeriod", signalPeriod);

        Strategy strategy = RuleGeneratorFactory.buildMacdStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runDmiStrategyBacktest(String symbol, String source, int period, int adxThreshold, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate DMI for period " + period);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("period", period);
        params.put("adxThreshold", adxThreshold);

        Strategy strategy = RuleGeneratorFactory.buildDmiStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runMfiStrategyBacktest(String symbol, String source, int period, int upperLimit, int lowerLimit, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate MFI for period " + period);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("period", period);
        params.put("upperLimit", upperLimit);
        params.put("lowerLimit", lowerLimit);

        Strategy strategy = RuleGeneratorFactory.buildMfiStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runTrendlineBreakoutStrategyBacktest(String symbol, String source, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("lookback", lookback);

        Strategy strategy = RuleGeneratorFactory.buildTrendlineBreakoutStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runEmaCrossoverStrategyBacktest(String symbol, String source, int shortPeriod, int longPeriod, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < longPeriod + 1) throw new NotEnoughDataException("Not enough bars to calculate EMA Crossover for period " + longPeriod);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("shortPeriod", shortPeriod);
        params.put("longPeriod", longPeriod);

        Strategy strategy = RuleGeneratorFactory.buildEMACrossoverStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runSmaCrossoverStrategyBacktest(String symbol, String source, int shortPeriod, int longPeriod, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < longPeriod + 1) throw new NotEnoughDataException("Not enough bars to calculate SMA Crossover for period " + longPeriod);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("shortPeriod", shortPeriod);
        params.put("longPeriod", longPeriod);

        Strategy strategy = RuleGeneratorFactory.buildSMACrossoverStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runSimpleSmaStrategyBacktest(String symbol, String source, int period, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate SMA for period " + period);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("period", period);

        Strategy strategy = RuleGeneratorFactory.simpleSmaStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    public N_StrategyBacktestDto runSimpleEmaStrategyBacktest(String symbol, String source, int period, int lookback){
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);

        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough bars to calculate EMA for period " + period);

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> params = new HashMap<>();
        params.put("period", period);

        Strategy strategy = RuleGeneratorFactory.simpleEmaStrategy(series, prices, params);
        BarSeriesManager seriesManager =  new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        return calculatePerformanceMetrics(symbol, strategy, series, tradingRecord, lookback);
    }

    /// ----------------- Custom Combined Strategies -----------------

    public N_StrategyBacktestDto runRsiMacdStrategyBacktest(String symbol, String source, int rsiPeriod, int rsiUpper, int rsiLower, int macdShort, int macdLong, int macdSignal, int lookback) {
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> rsiParams = new HashMap<>();
        rsiParams.put("period", rsiPeriod);
        rsiParams.put("upperLimit", rsiUpper);
        rsiParams.put("lowerLimit", rsiLower);
        Strategy rsi = RuleGeneratorFactory.buildRsiStrategy(series, prices, rsiParams);

        Map<String, Object> macdParams = new HashMap<>();
        macdParams.put("period", macdShort);
        macdParams.put("upperLimit", macdLong);
        macdParams.put("lowerLimit", macdSignal);
        Strategy macd = RuleGeneratorFactory.buildMacdStrategy(series, prices, macdParams);

        Rule entry = rsi.getEntryRule().and(macd.getEntryRule());
        Rule exit = rsi.getExitRule().or(macd.getExitRule());

        Strategy combined = new BaseStrategy("RSI + MACD Strategy", entry, exit);
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(combined);

        return calculatePerformanceMetrics(symbol, combined, series, record, lookback);
    }

    public N_StrategyBacktestDto runRsiBollingerStrategyBacktest(String symbol, String source, int rsiPeriod, int rsiUpper, int rsiLower, int bbPeriod, double stdDev, String maType, int lookback) {
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> rsiParams = new HashMap<>();
        rsiParams.put("period", rsiPeriod);
        rsiParams.put("upperLimit", rsiUpper);
        rsiParams.put("lowerLimit", rsiLower);
        Strategy rsi = RuleGeneratorFactory.buildRsiStrategy(series, prices, rsiParams);

        Map<String, Object> bbParams = new HashMap<>();
        bbParams.put("period", bbPeriod);
        bbParams.put("stdDev", stdDev);
        bbParams.put("maType", maType);
        Strategy bb = RuleGeneratorFactory.buildBollingerBandsStrategy(series, prices, bbParams);

        Rule entry = rsi.getEntryRule().and(bb.getEntryRule());
        Rule exit = rsi.getExitRule().or(bb.getExitRule());

        Strategy combined = new BaseStrategy("RSI + Bollinger Strategy", entry, exit);
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(combined);

        return calculatePerformanceMetrics(symbol, combined, series, record, lookback);
    }

    public N_StrategyBacktestDto runDmiEmaStrategyBacktest(String symbol, String source, int dmiPeriod, int adxThreshold, int emaShort, int emaLong, int lookback) {
        symbol = symbol.toUpperCase();
        BarSeries fullSeries = loadSeries(symbol);
        BarSeries series = (lookback > 0) ? sliceSeriesByLookback(fullSeries, lookback) : fullSeries;

        Indicator<Num> prices = sourceSelector(source, series);

        Map<String, Object> dmiParams = new HashMap<>();
        dmiParams.put("period", dmiPeriod);
        dmiParams.put("adxThreshold", adxThreshold);
        Strategy dmi = RuleGeneratorFactory.buildDmiStrategy(series, prices, dmiParams);

        Map<String, Object> emaParams = new HashMap<>();
        emaParams.put("period", emaShort);
        emaParams.put("upperLimit", emaLong);
        Strategy ema = RuleGeneratorFactory.buildEMACrossoverStrategy(series, prices, emaParams);

        Rule entry = dmi.getEntryRule().and(ema.getEntryRule());
        Rule exit = dmi.getExitRule().or(ema.getExitRule());

        Strategy combined = new BaseStrategy("DMI + EMA Strategy", entry, exit);
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(combined);

        return calculatePerformanceMetrics(symbol, combined, series, record, lookback);
    }

    /// ----------------- Performance Metrics DTO Builder -----------------

    public N_StrategyBacktestDto calculatePerformanceMetrics(String symbol, Strategy strategy, BarSeries series, TradingRecord tradingRecord, int lookback) {
        String signal = signalGenerator(strategy, series.getEndIndex());
        N_StrategyBacktestDto dto = new N_StrategyBacktestDto();
        dto.setSymbol(symbol != null ? symbol : "unknown");
        dto.setStrategyName(strategy != null ? strategy.getName() : "unknown");
        dto.setLookbackPeriod(lookback);
        dto.setCurrentSignal(signal);
        dto.setScore(scoreGenerator(signal));

        if (series == null || tradingRecord == null) return dto;

        try {
            Num n = new ProfitCriterion().calculate(series, tradingRecord);
            dto.setTotalProfit(n != null ? n.doubleValue() : 0.0);
        } catch (Exception ignored) {
            dto.setTotalProfit(0.0);
        }

        try {
            Num n = new ReturnCriterion().calculate(series, tradingRecord);
            dto.setGrossReturn(n != null ? n.doubleValue() : 0.0);
        } catch (Exception ignored) {
            dto.setGrossReturn(0.0);
        }

        try {
            Num n = new AverageProfitCriterion().calculate(series, tradingRecord);
            dto.setAverageProfit(n != null ? n.doubleValue() : 0.0);
        } catch (Exception ignored) {
            dto.setAverageProfit(0.0);
        }

        try {
            Num n = new ReturnOverMaxDrawdownCriterion().calculate(series, tradingRecord);
            double val = n != null ? n.doubleValue() : 0.0;
            dto.setReturnOverMaxDrawdown(val);
            dto.setRewardRiskRatio(val);
        } catch (Exception ignored) {
            dto.setReturnOverMaxDrawdown(0.0);
            dto.setRewardRiskRatio(0.0);
        }

        try {
            Num n = new MaximumDrawdownCriterion().calculate(series, tradingRecord);
            dto.setMaximumDrawdown(n != null ? n.doubleValue() : 0.0);
        } catch (Exception ignored) {
            dto.setMaximumDrawdown(0.0);
        }

        try {
            double avgDrawdown = computeAverageDrawdown(series, tradingRecord);
            dto.setAverageDrawdown(avgDrawdown);
        } catch (Exception ignored) {
            dto.setAverageDrawdown(0.0);
        }

        // ---------- counts ----------
        try {
            int trades = tradingRecord.getTrades() != null ? tradingRecord.getTrades().size() : 0;
            dto.setNumberOfTrades(trades);
        } catch (Exception e) {
            dto.setNumberOfTrades(tradingRecord.getTrades() != null ? tradingRecord.getTrades().size() : 0);
        }

        try {
            int posCount = new NumberOfPositionsCriterion().calculate(series, tradingRecord).intValue();
            dto.setNumberOfPositions(posCount);
        } catch (Exception e) {
            dto.setNumberOfPositions(tradingRecord.getPositionCount());
        }

        // ---------- positions / win-loss ----------
        double winPosRatio = 0.0;
        double lossPosRatio = 0.0;
        try {
            Num winNum = new PositionsRatioCriterion(AnalysisCriterion.PositionFilter.PROFIT).calculate(series, tradingRecord);
            Num lossNum = new PositionsRatioCriterion(AnalysisCriterion.PositionFilter.LOSS).calculate(series, tradingRecord);
            winPosRatio = winNum != null ? winNum.doubleValue() : 0.0;
            lossPosRatio = lossNum != null ? lossNum.doubleValue() : 0.0;
        } catch (Exception ignored) {
            // will fallback to manual counts below
        }

        double winningTradesRatio = 0.0;
        try {
            double wins = new NumberOfWinningPositionsCriterion().calculate(series, tradingRecord).doubleValue();
            double losses = new NumberOfLosingPositionsCriterion().calculate(series, tradingRecord).doubleValue();
            double denom = wins + losses;
            winningTradesRatio = denom > 0 ? wins / denom : 0.0;
        } catch (Exception ignored) {
            winningTradesRatio = 0.0; // fallback later
        }

        // ---------- manual position-level stats, holding periods, equity fallback ----------
        List<org.ta4j.core.Position> positions = tradingRecord.getPositions() != null ? tradingRecord.getPositions() : new ArrayList<>();
        int closedCount = 0;
        int winCount = 0;
        int loseCount = 0;
        double sumProfitPct = 0.0; // sum of pos returns (multiplicative - 1)
        double totalHoldingBars = 0.0;

        int barCount = Math.max(1, series.getBarCount());
        double[] equityFromPositions = new double[barCount];
        for (int i = 0; i < equityFromPositions.length; i++) equityFromPositions[i] = 1.0;
        double capital = 1.0;

        for (org.ta4j.core.Position pos : positions) {
            if (!pos.isClosed()) continue;
            closedCount++;

            Num posProfitNum = null;
            try {
                posProfitNum = pos.getProfit(); // ta4j 0.18
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
            if (winningTradesRatio == 0.0) {
                winningTradesRatio = (winCount + loseCount) > 0 ? (double) winCount / (winCount + loseCount) : 0.0;
            }
        } else {
            winPosRatio = 0.0;
            lossPosRatio = 0.0;
            winningTradesRatio = 0.0;
        }
        dto.setWinningPositionsRatio(winPosRatio);
        dto.setLosingPositionsRatio(lossPosRatio);
        dto.setWinningTradesRatio(winningTradesRatio);

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

        // ---------- GrossReturn vs buy&hold ----------
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

        // compute sharpe/sortino
        double mean = mean(perBarReturns);
        double std = std(perBarReturns);
        final double ANNUAL_FACTOR = Math.sqrt(252.0);
        double sharpe = std > 0 ? (mean / std) * ANNUAL_FACTOR : 0.0;
        dto.setSharpeRatio(sharpe);

        double downsideStd = downsideStd(perBarReturns);
        double sortino = downsideStd > 0 ? (mean / downsideStd) * ANNUAL_FACTOR : 0.0;
        dto.setSortinoRatio(sortino);

        TradingStatement tradingStatement = generator.generate(strategy, tradingRecord, series);

        dto.setTotalLoss(tradingStatement.getPerformanceReport().getTotalLoss().doubleValue());
        dto.setTotalProfit(tradingStatement.getPerformanceReport().getTotalProfit().doubleValue());
        dto.setTotalProfitLossRatio(tradingStatement.getPerformanceReport().getTotalProfitLoss().doubleValue());
        dto.setTotalProfitLossRatioPercent(tradingStatement.getPerformanceReport().getTotalProfitLossPercentage().doubleValue());

        dto.setBreakEvenCount(tradingStatement.getPositionStatsReport().getBreakEvenCount().doubleValue());
        dto.setLossCount(tradingStatement.getPositionStatsReport().getLossCount().doubleValue());
        dto.setProfitCount(tradingStatement.getPositionStatsReport().getProfitCount().doubleValue());

        dto.setLastDecisionValue(lastDecisionValueGenerator(dto.getScore(), tradingStatement.getPerformanceReport().getTotalProfitLossPercentage().doubleValue())); //TODO: Metric can be generic
        dto.setLastDecisionValueDescriptor(lastDecisionValueDescriptor(dto.getLastDecisionValue()));

        return dto;
    }

    /// ----------------- Helper Stats Methods -----------------

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

    private double computeAverageDrawdown(BarSeries series, TradingRecord tradingRecord) {
        try {
            CashFlow cf = new CashFlow(series, tradingRecord);
            int end = series.getEndIndex();
            if (end <= 0) return 0.0;

            List<Double> drawdowns = new ArrayList<>();
            Num first = cf.getValue(0);
            if (first == null || first.doubleValue() <= 0) return 0.0;
            double peak = first.doubleValue();
            double trough = peak;
            boolean inDrawdown = false;

            for (int i = 1; i <= end; i++) {
                Num valNum = cf.getValue(i);
                if (valNum == null) continue;
                double v = valNum.doubleValue();

                if (v > peak) {
                    // drawdown ended (if we were in one)
                    if (inDrawdown) {
                        if (peak > 0) {
                            double dd = (peak - trough) / peak; // relative drawdown
                            drawdowns.add(dd);
                        }
                        inDrawdown = false;
                    }
                    peak = v;
                    trough = v;
                } else {
                    // still in drawdown or starting one
                    inDrawdown = true;
                    if (v < trough) trough = v;
                }
            }
            // if still in drawdown at the end, close it
            if (inDrawdown && peak > 0) {
                drawdowns.add((peak - trough) / peak);
            }

            if (drawdowns.isEmpty()) return 0.0;
            double sum = 0.0;
            for (Double d : drawdowns) sum += d;
            return sum / drawdowns.size();
        } catch (Exception ex) {
            return 0.0;
        }
    }

    /// ----------------- Data Manipulation Helper Methods -----------------

    private String signalGenerator(Strategy strategy, int targetIndex) {
        if (strategy.shouldEnter(targetIndex)) {
            return "BUY";
        } else if (strategy.shouldExit(targetIndex)) {
            return "SELL";
        }
        return "NEUTRAL";
    }

    private double scoreGenerator(String signal){
        switch (signal) {
            case "BUY":
                return 1.0;
            case "SELL":
                return -1.0;
            default:
                return 0.0;
        }
    }

    double lastDecisionValueGenerator(double score, double metric){
        double normalizedMetric = Math.tanh(metric); // -1 ile 1 arasında sınırlar
        //double normalizedMetric = metric / 100; 
        //System.out.println("Metric Value: " + Math.round(metric) / 100.0 + "\t Normalized Metric Value: " + Math.round(normalizedMetric));
        return score * normalizedMetric;
    }

    String lastDecisionValueDescriptor(double value){
        if (value > 0.2) return "STRONG_BUY";
        else if (value > 0.05) return "BUY";
        else if (value > -0.05 && value < 0.05) return "HOLD";
        else if (value < -0.05) return "SELL";
        else return "WRONG_SIGNAL";
    }

    private BarSeries loadSeries(String symbol) {

        if (repository.findBySymbol(symbol).isEmpty()) {
            throw new NotEnoughDataException("No data found for symbol: " + symbol);
        }

        List<StockDataDaily> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);
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

    private Indicator<Num> sourceSelector(String priceType, BarSeries series) {
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

    private BarSeries sliceSeriesByLookback(BarSeries originalSeries, int lookbackBars) {
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
