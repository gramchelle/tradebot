package lion.mode.tradebot_backend.service.technicalanalysis.strategy.indicator;

import org.ta4j.core.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

public class BollingerBandsRuleFactory {

    private static final int DEFAULT_PERIOD = 20;
    private static final double DEFAULT_STD_DEV_MULTIPLIER = 2.0;
    private static final String DEFAULT_BASIS_MA_TYPE = "SMA";

    ///  Build basic Bollinger Bands Strategy
    public static Strategy buildBollingerBandsStrategy(BarSeries series, Indicator<Num> prices, Map<String, Object> params) {
        int period = ((Number) params.getOrDefault("period", DEFAULT_PERIOD)).intValue();
        double stdDev = ((Number) params.getOrDefault("stdDev", DEFAULT_STD_DEV_MULTIPLIER)).doubleValue();
        String maType = params.getOrDefault("maType", DEFAULT_BASIS_MA_TYPE).toString();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Indicator<Num> ma = maType.equalsIgnoreCase("EMA") ? new EMAIndicator(closePrice, period) : new SMAIndicator(closePrice, period);

        BollingerBandFacade bollinger = new BollingerBandFacade(series, period, stdDev);

        Rule entryRule = new UnderIndicatorRule(prices, bollinger.lower());
        Rule exitRule  = new OverIndicatorRule(prices, bollinger.upper());

        return new BaseStrategy("Bollinger Bands Strategy", entryRule, exitRule);
    }

}
