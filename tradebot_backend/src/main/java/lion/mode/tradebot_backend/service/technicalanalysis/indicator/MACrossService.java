package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import lion.mode.tradebot_backend.dto.indicator.MACrossResult;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.num.Num;

@Service
public class MACrossService extends IndicatorService {

    public MACrossService(StockDataRepository repository) {
        super(repository);
    }

    public MACrossResult calculateEMACross(String symbol, int shortPeriod, int longPeriod, LocalDateTime date, int lookback, String priceType) {
        BarSeries series = loadSeries(symbol);
        MACrossResult result = new MACrossResult();

        if (series.getBarCount() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MA Cross for " + symbol);
        }

        int targetIndex = seriesAmountValidator(symbol, series, date);

        Indicator<Num> prices = priceTypeSelector(priceType, series);
        EMAIndicator shortEma = new EMAIndicator(prices, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(prices, longPeriod);

        double shortMA = shortEma.getValue(targetIndex).doubleValue();
        double longMA  = longEma.getValue(targetIndex).doubleValue();

        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setMaType("EMA");
        result.setShortMAValue(shortMA);
        result.setLongMAValue(longMA);

        int startIndex = Math.max(1, targetIndex - lookback);
        int lastCrossIdx = -1;
        boolean lastCrossBullish = false;

        for (int i = startIndex; i <= targetIndex; i++) {
            if (i - 1 < 0 || i > series.getEndIndex()) continue;
            double prevDiff = shortEma.getValue(i - 1).doubleValue() - longEma.getValue(i - 1).doubleValue();
            double diff = shortEma.getValue(i).doubleValue() - longEma.getValue(i).doubleValue();
            if (prevDiff < 0 && diff > 0) {
                lastCrossIdx = i;
                lastCrossBullish = true;
            } else if (prevDiff > 0 && diff < 0) {
                lastCrossIdx = i;
                lastCrossBullish = false;
            }
        }

        double diffAtTarget = shortMA - longMA;
        double relThreshold = 0.005; // 0.5% relative threshold (tunable)
        double absThreshold = Math.max(1e-8, Math.abs(longMA) * relThreshold);

        if (lastCrossIdx != -1) {
            double crossDiff = shortEma.getValue(lastCrossIdx).doubleValue() - longEma.getValue(lastCrossIdx).doubleValue();
            result.setCrossoverPoint(crossDiff);
            if (lastCrossBullish) {
                if (diffAtTarget >= absThreshold) {
                    result.setSignal("Buy");
                } else {
                    result.setSignal("Hold");
                }
            } else {
                if (diffAtTarget <= -absThreshold) {
                    result.setSignal("Sell");
                } else {
                    result.setSignal("Hold");
                }
            }
        } else {
            result.setCrossoverPoint(diffAtTarget);
            if (diffAtTarget >= absThreshold) {
                result.setSignal("Buy");
            } else if (diffAtTarget <= -absThreshold) {
                result.setSignal("Sell");
            } else {
                result.setSignal("Hold");
            }
        }

        String sig = result.getSignal();
        result.setScore("Buy".equals(sig) ? 1 : "Sell".equals(sig) ? -1 : 0);

        return result;
    }

    public MACrossResult calculateSMACross(String symbol, int shortPeriod, int longPeriod, LocalDateTime date, int lookback, String priceType) {
        BarSeries series = loadSeries(symbol);
        MACrossResult result = new MACrossResult();

        if (series.getBarCount() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MA Cross for " + symbol);
        }

        int targetIndex = seriesAmountValidator(symbol, series, date);

        Indicator<Num> prices = priceTypeSelector(priceType, series);
        SMAIndicator shortSma = new SMAIndicator(prices, shortPeriod);
        SMAIndicator longSma = new SMAIndicator(prices, longPeriod);

        double shortMA = shortSma.getValue(targetIndex).doubleValue();
        double longMA  = longSma.getValue(targetIndex).doubleValue();

        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setMaType("SMA");
        result.setShortMAValue(shortMA);
        result.setLongMAValue(longMA);

        int startIndex = Math.max(1, targetIndex - lookback);
        int lastCrossIdx = -1;
        boolean lastCrossBullish = false;

        for (int i = startIndex; i <= targetIndex; i++) {
            if (i - 1 < 0 || i > series.getEndIndex()) continue;
            double prevDiff = shortSma.getValue(i - 1).doubleValue() - longSma.getValue(i - 1).doubleValue();
            double diff = shortSma.getValue(i).doubleValue() - longSma.getValue(i).doubleValue();
            if (prevDiff < 0 && diff > 0) {
                lastCrossIdx = i;
                lastCrossBullish = true;
            } else if (prevDiff > 0 && diff < 0) {
                lastCrossIdx = i;
                lastCrossBullish = false;
            }
        }

        double diffAtTarget = shortMA - longMA;
        double relThreshold = 0.005; // 0.5% relative threshold (tunable)
        double absThreshold = Math.max(1e-8, Math.abs(longMA) * relThreshold);

        if (lastCrossIdx != -1) {
            double crossDiff = shortSma.getValue(lastCrossIdx).doubleValue() - longSma.getValue(lastCrossIdx).doubleValue();
            result.setCrossoverPoint(crossDiff);
            if (lastCrossBullish) {
                if (diffAtTarget >= absThreshold) {
                    result.setSignal("Buy");
                } else {
                    result.setSignal("Hold");
                }
            } else {
                if (diffAtTarget <= -absThreshold) {
                    result.setSignal("Sell");
                } else {
                    result.setSignal("Hold");
                }
            }
        } else {
            result.setCrossoverPoint(diffAtTarget);
            if (diffAtTarget >= absThreshold) {
                result.setSignal("Buy");
            } else if (diffAtTarget <= -absThreshold) {
                result.setSignal("Sell");
            } else {
                result.setSignal("Hold");
            }
        }

        String sig = result.getSignal();
        result.setScore("Buy".equals(sig) ? 1 : "Sell".equals(sig) ? -1 : 0);

        return result;
    }



}
