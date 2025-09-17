package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.MACrossoverEntry;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.num.Num;

@Service
public class MACrossService extends IndicatorService{

    public MACrossService(StockDataRepository repository) {
        super(repository);
    }

    public BaseIndicatorResponse calculate(MACrossoverEntry entry) {
        BarSeries series = loadSeries(entry.getSymbol());
        return calculateMACrossWithSeries(entry, series);
    }

    public BaseIndicatorResponse calculateMACrossWithSeries(MACrossoverEntry entry, BarSeries series){
        String symbol = entry.getSymbol();
        int shortPeriod = entry.getShortPeriod();
        int longPeriod = entry.getLongPeriod();
        LocalDateTime date = entry.getDate();
        int lookback = entry.getLookback();
        String source = entry.getSource();
        String maType = entry.getMaType(); // SMA or EMA
        double relThreshold =  entry.getRelativeThreshold();

        if (series.getBarCount() < longPeriod) throw new IllegalArgumentException("Not enough data to calculate EMA Cross for " + symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);

        Indicator<Num> prices = sourceSelector(source, series);

        CachedIndicator<Num> shortMa, longMa;

        if (maType.equalsIgnoreCase("ema")) {
            shortMa = new EMAIndicator(prices, shortPeriod);
            longMa = new EMAIndicator(prices, longPeriod);
        } else if (maType.equalsIgnoreCase("sma")) {
            shortMa = new SMAIndicator(prices, shortPeriod);
            longMa = new SMAIndicator(prices, longPeriod);
        } else {
            throw new IllegalArgumentException("Invalid Moving Average Type " + maType);
        }

        double shortMA = shortMa.getValue(targetIndex).doubleValue();
        double longMA  = longMa.getValue(targetIndex).doubleValue();

        int startIndex = Math.max(1, targetIndex - lookback);
        int lastCrossIdx = -1;
        boolean lastCrossBullish = false;

        for (int i = startIndex; i <= targetIndex; i++) {
            if (i - 1 < 0 || i > series.getEndIndex()) continue;

            double prevDiff = shortMa.getValue(i - 1).doubleValue() - longMa.getValue(i - 1).doubleValue();
            double diff = shortMa.getValue(i).doubleValue() - longMa.getValue(i).doubleValue();

            if (prevDiff < 0 && diff > 0) {
                lastCrossIdx = i;
                lastCrossBullish = true;
            } else if (prevDiff > 0 && diff < 0) {
                lastCrossIdx = i;
                lastCrossBullish = false;
            }
        }

        double diffAtTarget = shortMA - longMA;
        double absThreshold = Math.max(1e-8, Math.abs(longMA) * relThreshold);

        BaseIndicatorResponse result = new BaseIndicatorResponse();
        result.setSymbol(symbol);
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());
        result.setIndicator(maType + "MA Crossover");
        result.getValues().put("shortPeriod", (double) shortPeriod);
        result.getValues().put("longPeriod", (double) longPeriod);
        result.getValues().put("shortMaValue", shortMA);
        result.getValues().put("longMaValue", longMA);

        if (lastCrossIdx != -1) {
            double crossDiff = shortMa.getValue(lastCrossIdx).doubleValue() - longMa.getValue(lastCrossIdx).doubleValue();
            result.getValues().put("crossoverPoint", crossDiff);
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
            result.getValues().put("crossoverPoint", diffAtTarget);
            if (diffAtTarget >= absThreshold) {
                result.setSignal("Buy");
            } else if (diffAtTarget <= -absThreshold) {
                result.setSignal("Sell");
            } else {
                result.setSignal("Hold");
            }
        }
        return result;
    }
}