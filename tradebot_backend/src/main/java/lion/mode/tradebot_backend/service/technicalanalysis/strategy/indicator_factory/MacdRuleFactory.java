package lion.mode.tradebot_backend.service.technicalanalysis.strategy.indicator;

import org.ta4j.core.*;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.util.Map;

public class MacdRuleFactory {

    private static final int DEFAULT_SHORT_PERIOD = 12;
    private static final int DEFAULT_LONG_PERIOD = 26;
    private static final int DEFAULT_SIGNAL_PERIOD = 9;

    ///  Build basic MACD Strategy
    public static Strategy buildMacdStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int shortPeriod = ((Number) params.getOrDefault("period", DEFAULT_SHORT_PERIOD)).intValue();
        int longPeriod = ((Number) params.getOrDefault("upperLimit", DEFAULT_LONG_PERIOD)).intValue();
        int signalPeriod = ((Number) params.getOrDefault("lowerLimit", DEFAULT_SIGNAL_PERIOD)).intValue();

        MACDIndicator macdIndicator = new MACDIndicator(prices, shortPeriod, longPeriod);

        Rule entryRule = new CrossedUpIndicatorRule(macdIndicator, macdIndicator.getSignalLine(signalPeriod));
        Rule exitRule  = new CrossedDownIndicatorRule(macdIndicator, macdIndicator.getSignalLine(signalPeriod));

        return new BaseStrategy("MACD Strategy", entryRule, exitRule);
    }
}
