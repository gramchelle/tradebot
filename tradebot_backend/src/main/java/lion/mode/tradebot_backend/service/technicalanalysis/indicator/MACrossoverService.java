package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.MACrossoverEntry;
import lion.mode.tradebot_backend.repository.StockDataRepository;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.num.Num;

@Service
public class MACrossoverService extends IndicatorService{

    public MACrossoverService(StockDataRepository repository) {
        super(repository);
    }

    public BaseIndicatorResponse calculate(MACrossoverEntry entry) {
        BarSeries series = loadSeries(entry.getSymbol().toUpperCase());
        return calculateWithSeries(entry, series);
    }

    public BaseIndicatorResponse calculateWithSeries(MACrossoverEntry entry, BarSeries series){
        String symbol = entry.getSymbol().toUpperCase();
        int shortPeriod = entry.getShortPeriod();
        int longPeriod = entry.getLongPeriod();
        Instant date = entry.getDate();
        int lookback = entry.getLookback();
        String source = entry.getSource();
        String maType = entry.getMaType(); // SMA or EMA
        double relThreshold = entry.getRelativeThreshold();

        if (series.getBarCount() < longPeriod) throw new IllegalArgumentException("Not enough data to calculate " + maType + " Cross for " + symbol);
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
            throw new IllegalArgumentException("Invalid Moving Average Type " + maType + ". Use 'SMA' or 'EMA'.");
        }

        double shortMA = shortMa.getValue(targetIndex).doubleValue();
        double longMA = longMa.getValue(targetIndex).doubleValue();

        int startIndex = Math.max(longPeriod, targetIndex - lookback);
        int lastCrossIdx = -1;
        boolean lastCrossBullish = false;

        for (int i = startIndex + 1; i <= targetIndex; i++) {
            if (i - 1 < longPeriod || i > series.getEndIndex()) continue;

            double prevShort = shortMa.getValue(i - 1).doubleValue();
            double prevLong = longMa.getValue(i - 1).doubleValue();
            double currShort = shortMa.getValue(i).doubleValue();
            double currLong = longMa.getValue(i).doubleValue();

            double prevDiff = prevShort - prevLong;
            double currDiff = currShort - currLong;

            // Bullish crossover
            if (prevDiff <= 0 && currDiff > 0) {
                lastCrossIdx = i;
                lastCrossBullish = true;
            }
            // Bearish crossover
            else if (prevDiff >= 0 && currDiff < 0) {
                lastCrossIdx = i;
                lastCrossBullish = false;
            }
        }

        double diffAtTarget = shortMA - longMA;
        double absThreshold = Math.max(1e-8, Math.abs(longMA) * relThreshold);

        BaseIndicatorResponse result = new BaseIndicatorResponse();
        result.setSymbol(symbol);
        result.setDate(series.getBar(targetIndex).getEndTime());
        result.setIndicator(maType.toUpperCase() + " Crossover");
        result.getValues().put("shortPeriod", (double) shortPeriod);
        result.getValues().put("longPeriod", (double) longPeriod);
        result.getValues().put("shortMaValue", shortMA);
        result.getValues().put("longMaValue", longMA);
        result.getValues().put("currentDifference", diffAtTarget);
        result.getValues().put("threshold", absThreshold);

    int barsSinceSignal = calculateBarsSinceSignal(shortMa, longMa, targetIndex);
    result.setBarsSinceSignal(barsSinceSignal);

    if (lastCrossIdx != -1) {
        double crossDiff = shortMa.getValue(lastCrossIdx).doubleValue() - longMa.getValue(lastCrossIdx).doubleValue();
        result.getValues().put("lastCrossoverDiff", crossDiff);
        result.getValues().put("crossoverBarsAgo", (double)(targetIndex - lastCrossIdx));
        String trend = lastCrossBullish ? "Bullish" : "Bearish";
        result.setStatus("crossoverType: " + trend);

        if (lastCrossBullish) {
            // Bullish crossover
            if (diffAtTarget >= absThreshold) {
                result.setSignal("Buy");
                result.setScore(0.8); // Strong bullish
            } else {
                result.setSignal("Hold"); // Crossover occurred but not strong enough
                result.setScore(0.3); // Weak bullish
            }
        } else {
            // Bearish crossover occurred
            if (diffAtTarget <= -absThreshold) {
                result.setSignal("Sell");
                result.setScore(-0.8); // Strong bearish signal
            } else {
                result.setSignal("Hold"); // Crossover occurred but not strong enough
                result.setScore(-0.3); // Weak bearish signal
            }
        }
    } else {
        result.setStatus("No recent crossover");
        if (diffAtTarget >= absThreshold) {
            result.setSignal("Buy");
            result.setScore(0.5); // Moderate bullish signal
        } else if (diffAtTarget <= -absThreshold) {
            result.setSignal("Sell");
            result.setScore(-0.5); // Moderate bearish signal
        } else {
            result.setSignal("Hold");
            result.setScore(0.0); // Neutral
        }
    }
        return result;
    }

    private int calculateBarsSinceSignal(CachedIndicator<Num> shortMa, CachedIndicator<Num> longMa, int targetIndex) {
        if (targetIndex <= 0) {
            return -1;
        }
        
        double currentShort = shortMa.getValue(targetIndex).doubleValue();
        double currentLong = longMa.getValue(targetIndex).doubleValue();
        
        double currentDiff = Math.abs(currentShort - currentLong);
        double relativeDiff = currentDiff / Math.max(currentLong, 1e-8); // Avoid division by zero
        
        if (relativeDiff < 0.01) {
            return -1;
        }
        
        boolean currentlyBullish = currentShort > currentLong;
        
        for (int i = targetIndex - 1; i >= 0; i--) {
            double prevShort = shortMa.getValue(i).doubleValue();
            double prevLong = longMa.getValue(i).doubleValue();
            boolean wasBullish = prevShort > prevLong;
            
            if (currentlyBullish != wasBullish) {
                return targetIndex - i;
            }
        }
        
        return -1;
    }

}