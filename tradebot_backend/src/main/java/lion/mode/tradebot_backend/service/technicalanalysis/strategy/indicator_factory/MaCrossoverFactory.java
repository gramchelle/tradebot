package lion.mode.tradebot_backend.service.technicalanalysis.strategy.indicator;

import org.ta4j.core.*;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.util.Map;

public class MaCrossoverFactory {

    private static final int DEFAULT_SHORT_PERIOD = 9;
    private static final int DEFAULT_LONG_PERIOD = 21;

    ///  Build basic SMA Crossover Strategy
    public static Strategy buildSMACrossoverStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", DEFAULT_LONG_PERIOD)).intValue();

        SMAIndicator shortSma = new SMAIndicator(prices, shortPeriod);
        SMAIndicator longSma = new SMAIndicator(prices, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule exitRule  = new CrossedDownIndicatorRule(shortSma, longSma);

        return new BaseStrategy("SMA Strategy", entryRule, exitRule);
    }

    ///  Build basic EMA Crossover Strategy
    public static Strategy buildEMACrossoverStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", DEFAULT_LONG_PERIOD)).intValue();

        EMAIndicator shortEma = new EMAIndicator(prices, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(prices, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(shortEma, longEma);
        Rule exitRule  = new CrossedDownIndicatorRule(shortEma, longEma);

        return new BaseStrategy("EMA Strategy", entryRule, exitRule);
    }

}
