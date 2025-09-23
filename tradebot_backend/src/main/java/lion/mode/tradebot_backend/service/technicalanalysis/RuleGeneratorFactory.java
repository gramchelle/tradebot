package lion.mode.tradebot_backend.service.technicalanalysis;

import org.ta4j.core.*;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.RecentSwingHighIndicator;
import org.ta4j.core.indicators.RecentSwingLowIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndexIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.TrailingStopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

public class RuleGeneratorFactory {

    private static final int DEFAULT_PERIOD = 14;
    private static final int RSI_DEFAULT_UPPER = 70;
    private static final int RSI_DEFAULT_LOWER = 30;

    private static final int MFI_DEFAULT_UPPER = 80;
    private static final int MFI_DEFAULT_LOWER = 20;

    private static final int MA_CROSSOVER_DEFAULT_SHORT_PERIOD = 9;
    private static final int MA_CROSSOVER_DEFAULT_LONG_PERIOD = 21;

    private static final int MACD_DEFAULT_SHORT_PERIOD = 12;
    private static final int MACD_DEFAULT_LONG_PERIOD = 26;
    private static final int MACD_DEFAULT_SIGNAL_PERIOD = 9;

    private static final int DEFAULT_ADX_THRESHOLD = 20;

    private static final int BB_DEFAULT_PERIOD = 20;
    private static final double DEFAULT_STD_DEV_MULTIPLIER = 2.0;
    private static final String DEFAULT_BASIS_MA_TYPE = "SMA";

    private static final int DEFAULT_SURROUNDING_BARS = 5;

    public static final double DEFAULT_STOP_LOSS = 1.0;
    public static final double DEFAULT_TAKE_PROFIT = 2.0;

    /* Basic Indicator-only strategies */

    public static Strategy buildRsiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", DEFAULT_PERIOD);
        int upperLimit = (int) params.getOrDefault("upperLimit", RSI_DEFAULT_UPPER);
        int lowerLimit = (int) params.getOrDefault("lowerLimit", RSI_DEFAULT_LOWER);
        double stopLoss = (double) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS);
        double takeProfit = (double) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT);

        RSIIndicator rsi = new RSIIndicator(prices, period);

        Rule entryRule = new UnderIndicatorRule(rsi, lowerLimit);
        Rule exitRule  = new OverIndicatorRule(rsi, upperLimit);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("RSI-Only Strategy", entryRule, exitRule);

        strategy.setUnstableBars(period);

        return strategy;
    }

    public static Strategy buildMfiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", DEFAULT_PERIOD);
        int upperLimit = (int) params.getOrDefault("upperLimit", MFI_DEFAULT_UPPER);
        int lowerLimit = (int) params.getOrDefault("lowerLimit", MFI_DEFAULT_LOWER);
        double stopLoss = (double) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS);
        double takeProfit = (double) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT);

        MoneyFlowIndexIndicator mfi = new MoneyFlowIndexIndicator(series, period);

        Rule entryRule = new UnderIndicatorRule(mfi, lowerLimit);
        Rule exitRule  = new OverIndicatorRule(mfi, upperLimit);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("MFI-Only Strategy", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    public static Strategy buildSMACrossoverStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", MA_CROSSOVER_DEFAULT_LONG_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();

        SMAIndicator shortSma = new SMAIndicator(prices, shortPeriod);
        SMAIndicator longSma = new SMAIndicator(prices, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule exitRule  = new CrossedDownIndicatorRule(shortSma, longSma);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("SMA Crossover Strategy", entryRule, exitRule);
        strategy.setUnstableBars(longPeriod);

        return strategy;
    }

    public static Strategy buildEMACrossoverStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", MA_CROSSOVER_DEFAULT_LONG_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();

        EMAIndicator shortEma = new EMAIndicator(prices, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(prices, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(shortEma, longEma);
        Rule exitRule  = new CrossedDownIndicatorRule(shortEma, longEma);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("EMA Crossover Strategy", entryRule, exitRule);
        strategy.setUnstableBars(longPeriod);

        return strategy;
    }

    public static Strategy buildMacdStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", MACD_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", MACD_DEFAULT_LONG_PERIOD)).intValue();
        int signalPeriod = ((Number) params.getOrDefault("lowerLimit", MACD_DEFAULT_SIGNAL_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();

        MACDIndicator macdIndicator = new MACDIndicator(prices, shortPeriod, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(macdIndicator, macdIndicator.getSignalLine(signalPeriod));
        Rule exitRule  = new CrossedDownIndicatorRule(macdIndicator, macdIndicator.getSignalLine(signalPeriod));

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("MACD-Only Strategy", entryRule, exitRule);
        strategy.setUnstableBars(longPeriod);

        return strategy;
    }

    public static Strategy buildDmiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", DEFAULT_PERIOD)).intValue();
        int adxThreshold = ((Number) params.getOrDefault("adxThreshold", DEFAULT_ADX_THRESHOLD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();

        PlusDIIndicator plusDi = new PlusDIIndicator(series, period);
        MinusDIIndicator minusDi = new MinusDIIndicator(series, period);
        ADXIndicator adx = new ADXIndicator(series, period);

        Rule entryRule = new CrossedUpIndicatorRule(plusDi, minusDi)
                .and(new OverIndicatorRule(adx, adxThreshold));

        Rule exitRule  = new CrossedDownIndicatorRule(plusDi, minusDi)
                .and(new UnderIndicatorRule(adx, adxThreshold));

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("DMI-Only Strategy", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    public static Strategy buildBollingerBandsStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", BB_DEFAULT_PERIOD)).intValue();
        double stdDev = ((Number) params.getOrDefault("stdDev", DEFAULT_STD_DEV_MULTIPLIER)).doubleValue();
        String maType = params.getOrDefault("maType", DEFAULT_BASIS_MA_TYPE).toString();
        double stopLoss = (double) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS);
        double takeProfit = (double) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Indicator<Num> ma = maType.equalsIgnoreCase("EMA") ? new EMAIndicator(closePrice, period) : new SMAIndicator(closePrice, period);

        BollingerBandFacade bollinger = new BollingerBandFacade(series, period, stdDev);

        Rule entryRule = new UnderIndicatorRule(prices, bollinger.lower());
        Rule exitRule  = new OverIndicatorRule(prices, bollinger.upper());

        BaseStrategy strategy = new BaseStrategy("Bollinger Bands-Only Strategy", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    public static Strategy buildTrendlineBreakoutStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int surroundingBars = ((Number) params.getOrDefault("surroundingBars", DEFAULT_SURROUNDING_BARS)).intValue();
        double stopLoss = (double) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS);
        double takeProfit = (double) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT);

        RecentSwingHighIndicator swingHighIndicator = new RecentSwingHighIndicator(series, surroundingBars);
        RecentSwingLowIndicator  swingLowIndicator  = new RecentSwingLowIndicator(series, surroundingBars);

        Rule entryRule = new OverIndicatorRule(swingHighIndicator, prices);
        Rule exitRule  = new UnderIndicatorRule(swingLowIndicator, prices);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("Trendline Breakout Strategy", entryRule, exitRule);
        strategy.setUnstableBars(surroundingBars);

        return strategy;
    }

    public static Strategy simpleSmaStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();   

        SMAIndicator sma = new SMAIndicator(prices, period);

        Rule entryRule = new CrossedUpIndicatorRule(sma, prices);
        Rule exitRule  = new CrossedDownIndicatorRule(sma, prices);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("SMA-Based Strategy", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    public static Strategy simpleEmaStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();

        EMAIndicator ema = new EMAIndicator(prices, period);

        Rule entryRule = new CrossedUpIndicatorRule(ema, prices);
        Rule exitRule  = new CrossedDownIndicatorRule(ema, prices);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit);

        BaseStrategy strategy = new BaseStrategy("EMA-Based Strategy", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    // Custom strategies


    // Helper 

    private static Rule applyOptionalExitRules(BarSeries series, Rule exitRule, double stopLoss, double takeProfit) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        if (stopLoss > 0) {
            StopLossRule slRule = new StopLossRule(closePrice, DecimalNum.valueOf(stopLoss));
            TrailingStopLossRule tslRule = new TrailingStopLossRule(closePrice, DecimalNum.valueOf(stopLoss));
            exitRule = exitRule.or(slRule).or(tslRule);
        }

        if (takeProfit > 0) {
            StopGainRule tpRule = new StopGainRule(closePrice, DecimalNum.valueOf(takeProfit));
            exitRule = exitRule.or(tpRule);
        }

        return exitRule;
    }
}
