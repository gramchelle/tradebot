package lion.mode.tradebot_backend.service.technicalanalysis.strategy.indicator;

import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

public class RsiRuleFactory {

    private static final int DEFAULT_PERIOD = 14;
    private static final int DEFAULT_UPPER = 70;
    private static final int DEFAULT_LOWER = 30;

    ///  Build basic RSI Strategy
    public static Strategy buildRsiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", DEFAULT_PERIOD);
        int upperLimit = (int) params.getOrDefault("upperLimit", DEFAULT_UPPER);
        int lowerLimit = (int) params.getOrDefault("lowerLimit", DEFAULT_LOWER);

        RSIIndicator rsi = new RSIIndicator(prices, period);

        Rule entryRule = new UnderIndicatorRule(rsi, lowerLimit);  // RSI < lowerLimit → Buy
        Rule exitRule  = new OverIndicatorRule(rsi, upperLimit);   // RSI > upperLimit → Sell

        return new BaseStrategy("RSI Strategy", entryRule, exitRule);
    }
}
