package lion.mode.tradebot_backend.service.technicalanalysis.strategy.indicator;

import org.ta4j.core.*;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

public class DmiRuleFactory {

    private static final int DEFAULT_PERIOD = 14;
    private static final int DEFAULT_ADX_THRESHOLD = 20;

    ///  Build basic DMI Strategy
    public static Strategy buildMacdStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
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

}
