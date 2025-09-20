package lion.mode.tradebot_backend.service.N_technicalanalysis;

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
import org.ta4j.core.indicators.trend.DownTrendIndicator;
import org.ta4j.core.indicators.trend.UpTrendIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndexIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
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

    /*
     * Basic Indicator-only strategies
     */

    ///  RSI-only
    public static Strategy buildRsiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", DEFAULT_PERIOD);
        int upperLimit = (int) params.getOrDefault("upperLimit", RSI_DEFAULT_UPPER);
        int lowerLimit = (int) params.getOrDefault("lowerLimit", RSI_DEFAULT_LOWER);

        RSIIndicator rsi = new RSIIndicator(prices, period);

        Rule entryRule = new UnderIndicatorRule(rsi, lowerLimit);
        Rule exitRule  = new OverIndicatorRule(rsi, upperLimit);

        return new BaseStrategy("RSI Strategy", entryRule, exitRule);
    }

    ///  MFI-Only
    public static Strategy buildMfiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", DEFAULT_PERIOD);
        int upperLimit = (int) params.getOrDefault("upperLimit", MFI_DEFAULT_UPPER);
        int lowerLimit = (int) params.getOrDefault("lowerLimit", MFI_DEFAULT_LOWER);

        MoneyFlowIndexIndicator mfi = new MoneyFlowIndexIndicator(series, period);

        Rule entryRule = new UnderIndicatorRule(mfi, lowerLimit);
        Rule exitRule  = new OverIndicatorRule(mfi, upperLimit);

        return new BaseStrategy("MFI Strategy", entryRule, exitRule);
    }

    ///  Build basic SMA Crossover Strategy
    public static Strategy buildSMACrossoverStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", MA_CROSSOVER_DEFAULT_LONG_PERIOD)).intValue();

        SMAIndicator shortSma = new SMAIndicator(prices, shortPeriod);
        SMAIndicator longSma = new SMAIndicator(prices, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule exitRule  = new CrossedDownIndicatorRule(shortSma, longSma);

        return new BaseStrategy("SMA Strategy", entryRule, exitRule);
    }

    ///  Build basic EMA Crossover Strategy
    public static Strategy buildEMACrossoverStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", MA_CROSSOVER_DEFAULT_LONG_PERIOD)).intValue();

        EMAIndicator shortEma = new EMAIndicator(prices, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(prices, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(shortEma, longEma);
        Rule exitRule  = new CrossedDownIndicatorRule(shortEma, longEma);

        return new BaseStrategy("EMA Strategy", entryRule, exitRule);
    }

    ///  Build basic MACD Strategy
    public static Strategy buildMacdStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", MACD_DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", MACD_DEFAULT_LONG_PERIOD)).intValue();
        int signalPeriod = ((Number) params.getOrDefault("lowerLimit", MACD_DEFAULT_SIGNAL_PERIOD)).intValue();

        MACDIndicator macdIndicator = new MACDIndicator(prices, shortPeriod, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(macdIndicator, macdIndicator.getSignalLine(signalPeriod));
        Rule exitRule  = new CrossedDownIndicatorRule(macdIndicator, macdIndicator.getSignalLine(signalPeriod));

        return new BaseStrategy("MACD Strategy", entryRule, exitRule);
    }

    /// DMI-only
    public static Strategy buildDmiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", DEFAULT_PERIOD)).intValue();
        int adxThreshold = ((Number) params.getOrDefault("adxThreshold", DEFAULT_ADX_THRESHOLD)).intValue();

        PlusDIIndicator plusDi = new PlusDIIndicator(series, period);
        MinusDIIndicator minusDi = new MinusDIIndicator(series, period);
        ADXIndicator adx = new ADXIndicator(series, period);

        Rule entryRule = new CrossedUpIndicatorRule(plusDi, minusDi)
                .and(new OverIndicatorRule(adx, adxThreshold));

        Rule exitRule  = new CrossedDownIndicatorRule(plusDi, minusDi)
                .and(new UnderIndicatorRule(adx, adxThreshold));

        return new BaseStrategy("DMI Strategy", entryRule, exitRule);
    }

    ///  Build basic Bollinger Bands Strategy
    public static Strategy buildBollingerBandsStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", BB_DEFAULT_PERIOD)).intValue();
        double stdDev = ((Number) params.getOrDefault("stdDev", DEFAULT_STD_DEV_MULTIPLIER)).doubleValue();
        String maType = params.getOrDefault("maType", DEFAULT_BASIS_MA_TYPE).toString();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Indicator<Num> ma = maType.equalsIgnoreCase("EMA") ? new EMAIndicator(closePrice, period) : new SMAIndicator(closePrice, period);

        BollingerBandFacade bollinger = new BollingerBandFacade(series, period, stdDev);

        Rule entryRule = new UnderIndicatorRule(prices, bollinger.lower());
        Rule exitRule  = new OverIndicatorRule(prices, bollinger.upper());

        return new BaseStrategy("Bollinger Bands Strategy", entryRule, exitRule);
    }

    public static Strategy buildTrendlineBreakoutStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        RecentSwingHighIndicator swingHighIndicator = new RecentSwingHighIndicator(series, 7);
        RecentSwingLowIndicator  swingLowIndicator  = new RecentSwingLowIndicator(series, 7);

        Rule entryRule = new OverIndicatorRule(swingHighIndicator, prices);
        Rule exitRule  = new UnderIndicatorRule(swingLowIndicator, prices);

        return new BaseStrategy("Trendline Breakout Strategy", entryRule, exitRule);
    }

    ///  Build basic SMA Crossover Strategy
    public static Strategy simpleSmaStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();

        SMAIndicator sma = new SMAIndicator(prices, period);

        Rule entryRule = new CrossedUpIndicatorRule(sma, prices);
        Rule exitRule  = new CrossedDownIndicatorRule(sma, prices);

        return new BaseStrategy("SMA Strategy", entryRule, exitRule);
    }

    public static Strategy simpleEmaStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", MA_CROSSOVER_DEFAULT_SHORT_PERIOD)).intValue();

        EMAIndicator ema = new EMAIndicator(prices, period);

        Rule entryRule = new CrossedUpIndicatorRule(ema, prices);
        Rule exitRule  = new CrossedDownIndicatorRule(ema, prices);

        return new BaseStrategy("EMA Strategy", entryRule, exitRule);
    }

    // Custom strategies
    // TODO: Add more custom strategies as needed

}
