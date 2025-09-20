package lion.mode.tradebot_backend.service.technicalanalysis.strategy.indicator;

import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndexIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

public class MfiRuleFactory {

    private static final int DEFAULT_PERIOD = 14;
    private static final int DEFAULT_UPPER = 80;
    private static final int DEFAULT_LOWER = 20;

    ///  Build basic MFI Strategy
    public static Strategy buildMfiStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", DEFAULT_PERIOD);
        int upperLimit = (int) params.getOrDefault("upperLimit", DEFAULT_UPPER);
        int lowerLimit = (int) params.getOrDefault("lowerLimit", DEFAULT_LOWER);

        MoneyFlowIndexIndicator mfi = new MoneyFlowIndexIndicator(series, period);

        Rule entryRule = new UnderIndicatorRule(mfi, lowerLimit);
        Rule exitRule  = new OverIndicatorRule(mfi, upperLimit);

        return new BaseStrategy("MFI Strategy", entryRule, exitRule);
    }

}
