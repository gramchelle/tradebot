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
import org.ta4j.core.rules.*;

import java.util.Map;

public class RuleGeneratorFactory {

    /// Initializing default parameters for strategies
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

    private static final int DEFAULT_SURROUNDING_BARS = 5;

    public static final double DEFAULT_STOP_LOSS = 0.0;
    public static final double DEFAULT_TAKE_PROFIT = 0.0;

    public static final int TRAILING_STOP_LOSS_INACTIVE = 0;

    /* Strategy Builders: returns Strategy objects based on the selected technical analysis method and parameters */
    
    // Oscillators

    public static Strategy buildRsiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("rsiPeriod", DEFAULT_PERIOD)).intValue();
        int upperLimit = ((Number) params.getOrDefault("rsiUpperLimit", RSI_DEFAULT_UPPER)).intValue();
        int lowerLimit = ((Number) params.getOrDefault("rsiLowerLimit", RSI_DEFAULT_LOWER)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        RSIIndicator rsi = new RSIIndicator(prices, period);

        Rule entryRule = new UnderIndicatorRule(rsi, lowerLimit);
        Rule exitRule  = new OverIndicatorRule(rsi, upperLimit);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("rsi-only", entryRule, exitRule);

        strategy.setUnstableBars(period);

        return strategy;
    }

    public static Strategy buildMfiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("mfiPeriod", DEFAULT_PERIOD)).intValue();
        int upperLimit = ((Number) params.getOrDefault("mfiUpperLimit", MFI_DEFAULT_UPPER)).intValue();
        int lowerLimit = ((Number) params.getOrDefault("mfiLowerLimit", MFI_DEFAULT_LOWER)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        MoneyFlowIndexIndicator mfi = new MoneyFlowIndexIndicator(series, period);

        Rule entryRule = new UnderIndicatorRule(mfi, lowerLimit);
        Rule exitRule  = new OverIndicatorRule(mfi, upperLimit);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("mfi-only", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    // Moving Averages Crossovers
    
    public static Strategy buildSMACrossoverStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("smaCrossoverShortPeriod", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("smaCrossoverLongPeriod", MA_CROSSOVER_DEFAULT_LONG_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        SMAIndicator shortSma = new SMAIndicator(prices, shortPeriod);
        SMAIndicator longSma = new SMAIndicator(prices, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule exitRule  = new CrossedDownIndicatorRule(shortSma, longSma);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("sma-crossover", entryRule, exitRule);
        strategy.setUnstableBars(longPeriod);

        return strategy;
    }

    public static Strategy buildEMACrossoverStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("emaCrossoverShortPeriod", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("emaCrossoverLongPeriod", MA_CROSSOVER_DEFAULT_LONG_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        EMAIndicator shortEma = new EMAIndicator(prices, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(prices, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(shortEma, longEma);
        Rule exitRule  = new CrossedDownIndicatorRule(shortEma, longEma);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("ema-crossover", entryRule, exitRule);
        strategy.setUnstableBars(longPeriod);

        return strategy;
    }

    // Trend Indicators: MACD, DMI

    public static Strategy buildMacdStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("macdShortPeriod", MACD_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("macdLongPeriod", MACD_DEFAULT_LONG_PERIOD)).intValue();
        int signalPeriod = ((Number) params.getOrDefault("macdSignalPeriod", MACD_DEFAULT_SIGNAL_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        MACDIndicator macdIndicator = new MACDIndicator(prices, shortPeriod, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(macdIndicator, macdIndicator.getSignalLine(signalPeriod));
        Rule exitRule  = new CrossedDownIndicatorRule(macdIndicator, macdIndicator.getSignalLine(signalPeriod));

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("macd-only", entryRule, exitRule);
        strategy.setUnstableBars(longPeriod);

        return strategy;
    }

    public static Strategy buildDmiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("dmiPeriod", DEFAULT_PERIOD)).intValue();
        int adxThreshold = ((Number) params.getOrDefault("dmiAdxThreshold", DEFAULT_ADX_THRESHOLD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        PlusDIIndicator plusDi = new PlusDIIndicator(series, period);
        MinusDIIndicator minusDi = new MinusDIIndicator(series, period);
        ADXIndicator adx = new ADXIndicator(series, period);

        Rule entryRule = new CrossedUpIndicatorRule(plusDi, minusDi)
                .and(new OverIndicatorRule(adx, adxThreshold));

        Rule exitRule  = new CrossedDownIndicatorRule(plusDi, minusDi)
                .and(new UnderIndicatorRule(adx, adxThreshold));

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("dmi-only", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    // Price Action: Trendline Breakout

    public static Strategy buildTrendlineBreakoutStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int surroundingBars = ((Number) params.getOrDefault("trendlineSurroundingBars", DEFAULT_SURROUNDING_BARS)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        RecentSwingHighIndicator swingHighIndicator = new RecentSwingHighIndicator(series, surroundingBars);
        RecentSwingLowIndicator  swingLowIndicator  = new RecentSwingLowIndicator(series, surroundingBars);

        Rule entryRule = new OverIndicatorRule(swingHighIndicator, prices);
        Rule exitRule  = new UnderIndicatorRule(swingLowIndicator, prices);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("trendline", entryRule, exitRule);
        strategy.setUnstableBars(surroundingBars);

        return strategy;
    }

    // Volatility Indicators: Bollinger Bands
    
    public static Strategy buildBollingerBandsStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("bollingerPeriod", BB_DEFAULT_PERIOD)).intValue();
        double stdDev = ((Number) params.getOrDefault("bollingerStdDev", DEFAULT_STD_DEV_MULTIPLIER)).doubleValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        BollingerBandFacade bollinger = new BollingerBandFacade(series, period, stdDev);

        Rule entryRule = new UnderIndicatorRule(prices, bollinger.lower());
        Rule exitRule  = new OverIndicatorRule(prices, bollinger.upper());

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("bollinger-bands-only", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    public static Strategy simpleSmaStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("smaPeriod", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();   
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        SMAIndicator sma = new SMAIndicator(prices, period);

        Rule entryRule = new CrossedUpIndicatorRule(sma, prices);
        Rule exitRule  = new CrossedDownIndicatorRule(sma, prices);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("sma-only", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    public static Strategy simpleEmaStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("emaPeriod", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        double stopLoss = ((Number) params.getOrDefault("stopLoss", DEFAULT_STOP_LOSS)).doubleValue();
        double takeProfit = ((Number) params.getOrDefault("takeProfit", DEFAULT_TAKE_PROFIT)).doubleValue();
        int trailingStopLoss = ((Number) params.getOrDefault("trailingStopLoss", TRAILING_STOP_LOSS_INACTIVE)).intValue();

        EMAIndicator ema = new EMAIndicator(prices, period);

        Rule entryRule = new CrossedUpIndicatorRule(ema, prices);
        Rule exitRule  = new CrossedDownIndicatorRule(ema, prices);

        exitRule = applyOptionalExitRules(series, exitRule, stopLoss, takeProfit, trailingStopLoss);

        BaseStrategy strategy = new BaseStrategy("ema-only", entryRule, exitRule);
        strategy.setUnstableBars(period);

        return strategy;
    }

    // For fixed period strategies, the period parameter is overridden by the value in params map

    public static Strategy buildFixedEmaStrategy(BarSeries series, Indicator<Num> prices, int period, Map<String, Object> params) {
        period = ((Number) params.getOrDefault("emaPeriod", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        Strategy strategy = simpleEmaStrategy(series, prices, params);
        return strategy;
    }    

    public static Strategy buildFixedSmaStrategy(BarSeries series, Indicator<Num> prices, int period, Map<String, Object> params) {
        period = ((Number) params.getOrDefault("smaPeriod", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        Strategy strategy = simpleSmaStrategy(series, prices, params);
        return strategy;
    }

    // Helpers

    private static boolean isTrailingLossPreferred(int answer) {
        if (answer == 1) return true;
        else if (answer == 0) return false;
        throw new IllegalArgumentException("Invalid input for trailing stop loss preference. Use 1 for yes, 0 for no.");
    }

    private static Rule applyOptionalExitRules(BarSeries series, Rule exitRule, double stopLoss, double takeProfit, int trailingStopLoss) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        if (stopLoss > 0) {
            StopLossRule slRule = new StopLossRule(closePrice, DecimalNum.valueOf(stopLoss));
            exitRule = exitRule.or(slRule);
            if (isTrailingLossPreferred(trailingStopLoss)) {
                TrailingStopLossRule tslRule = new TrailingStopLossRule(closePrice, DecimalNum.valueOf(stopLoss));
                exitRule = exitRule.or(tslRule);
            }
        }

        if (takeProfit > 0) {
            StopGainRule tpRule = new StopGainRule(closePrice, DecimalNum.valueOf(takeProfit));
            exitRule = exitRule.or(tpRule);
        }

        return exitRule;
    }
}
