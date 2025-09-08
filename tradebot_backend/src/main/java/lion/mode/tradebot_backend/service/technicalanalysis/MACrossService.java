package lion.mode.tradebot_backend.service.technicalanalysis;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import lion.mode.tradebot_backend.dto.indicators.MACrossResult;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;

@Service
public class MACrossService extends IndicatorService {

    public MACrossService(StockDataRepository repository) {
        super(repository);
    }

    public MACrossResult calculateEMACross(String symbol, int shortPeriod, int longPeriod) {
        BarSeries series = loadSeries(symbol);
        MACrossResult result = new MACrossResult();

        if (series.getBarCount() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MA Cross for " + symbol);
        }

        EMAIndicator shortEma = new EMAIndicator(new ClosePriceIndicator(series), shortPeriod);
        EMAIndicator longEma = new EMAIndicator(new ClosePriceIndicator(series), longPeriod);
        MACDIndicator macd = new MACDIndicator(new ClosePriceIndicator(series), shortPeriod, longPeriod);

        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setMaType("EMA");

        result.setShortMAValue(shortEma.getValue(series.getEndIndex()).doubleValue());
        result.setLongMAValue(longEma.getValue(series.getEndIndex()).doubleValue());
        result.setCrossoverPoint(macd.getValue(series.getEndIndex()).doubleValue());
        result.setSignal(macd.getValue(series.getEndIndex()).doubleValue() > 0 ? "Buy" : "Sell");
        result.setScore(result.getSignal().equals("Buy") ? 1 : -1);

        return result;
    }

    public MACrossResult calculateEMACrossUntil(String symbol, int shortPeriod, int longPeriod,
                                                LocalDateTime date, int lookback) {
        BarSeries series = loadSeries(symbol);
        MACrossResult result = new MACrossResult();

        if (series.getBarCount() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MA Cross for " + symbol);
        }

        int targetIndex = seriesAmountValidator(symbol, series, date);

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        EMAIndicator shortEma = new EMAIndicator(close, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(close, longPeriod);

        double shortMA = shortEma.getValue(targetIndex).doubleValue();
        double longMA  = longEma.getValue(targetIndex).doubleValue();

        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setMaType("EMA");
        result.setShortMAValue(shortMA);
        result.setLongMAValue(longMA);

        // compute lookback window (ensure we have previous value for diff calculation)
        // ensure startIndex >= 1 so (i-1) is valid
        int startIndex = Math.max(1, targetIndex - lookback);
        int lastCrossIdx = -1;
        boolean lastCrossBullish = false;

        // scan for last sign change of (shortEma - longEma) in window
        for (int i = startIndex; i <= targetIndex; i++) {
            // guard against invalid indices
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

        // threshold relative to longMA to avoid tiny noise triggers
        double diffAtTarget = shortMA - longMA;
        double relThreshold = 0.005; // 0.5% relative threshold (tunable)
        double absThreshold = Math.max(1e-8, Math.abs(longMA) * relThreshold);

        if (lastCrossIdx != -1) {
            // use the last crossover found within lookback
            double crossDiff = shortEma.getValue(lastCrossIdx).doubleValue() - longEma.getValue(lastCrossIdx).doubleValue();
            result.setCrossoverPoint(crossDiff);
            // require confirmation: current diff should be in same direction and above threshold
            if (lastCrossBullish) {
                if (diffAtTarget >= absThreshold) {
                    result.setSignal("Buy");
                } else {
                    // crossover happened but not yet confirmed
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
            // no crossover found in lookback -> evaluate current relationship with threshold
            result.setCrossoverPoint(diffAtTarget);
            if (diffAtTarget >= absThreshold) {
                result.setSignal("Buy");
            } else if (diffAtTarget <= -absThreshold) {
                result.setSignal("Sell");
            } else {
                result.setSignal("Hold");
            }
        }

        // score: 1 buy, -1 sell, 0 hold
        String sig = result.getSignal();
        result.setScore("Buy".equals(sig) ? 1 : "Sell".equals(sig) ? -1 : 0);

        return result;
    }

    public MACrossResult calculateSMACross(String symbol, int shortPeriod, int longPeriod) {
        BarSeries series = loadSeries(symbol);
        MACrossResult result = new MACrossResult();

        if (series.getBarCount() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MA Cross for " + symbol);
        }

        SMAIndicator shortSma = new SMAIndicator(new ClosePriceIndicator(series), shortPeriod);
        SMAIndicator longSma = new SMAIndicator(new ClosePriceIndicator(series), longPeriod);
        MACDIndicator macd = new MACDIndicator(new ClosePriceIndicator(series), shortPeriod, longPeriod);

        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setMaType("SMA");

        result.setShortMAValue(shortSma.getValue(series.getEndIndex()).doubleValue());
        result.setLongMAValue(longSma.getValue(series.getEndIndex()).doubleValue());
        result.setCrossoverPoint(macd.getValue(series.getEndIndex()).doubleValue());
        result.setSignal(macd.getValue(series.getEndIndex()).doubleValue() > 0 ? "Buy" : "Sell");
        result.setScore(result.getSignal().equals("Buy") ? 1 : -1);

        return result;
    }

    public MACrossResult calculateMaCrossUntil(String symbol, int shortPeriod, int longPeriod,
                                            LocalDateTime targetDate, int lookback, String maType) {
        BarSeries series = loadSeries(symbol);
        MACrossResult result = new MACrossResult();

        if (series.getBarCount() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MA Cross for " + symbol);
        }

        int targetIndex = seriesAmountValidator(symbol, series, targetDate);

        ClosePriceIndicator close = new ClosePriceIndicator(series);

        double shortMA, longMA, crossover;
        String signal;

        if (maType.equalsIgnoreCase("EMA")) {
            EMAIndicator shortEma = new EMAIndicator(close, shortPeriod);
            EMAIndicator longEma = new EMAIndicator(close, longPeriod);
            shortMA = shortEma.getValue(targetIndex).doubleValue();
            longMA  = longEma.getValue(targetIndex).doubleValue();
            crossover = shortMA - longMA;
        } else if (maType.equalsIgnoreCase("SMA")) {
            SMAIndicator shortSma = new SMAIndicator(close, shortPeriod);
            SMAIndicator longSma = new SMAIndicator(close, longPeriod);
            shortMA = shortSma.getValue(targetIndex).doubleValue();
            longMA  = longSma.getValue(targetIndex).doubleValue();
            crossover = shortMA - longMA;
        } else {
            throw new IllegalArgumentException("Invalid MA type: " + maType);
        }

        signal = crossover > 0 ? "Buy" : "Sell";

        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setMaType(maType.toUpperCase());
        result.setShortMAValue(shortMA);
        result.setLongMAValue(longMA);
        //result.setMaType("EMA"); // THIS LINE WILL BE REMOVED AFTER TESTING
        result.setCrossoverPoint(crossover);
        result.setSignal(signal);
        result.setScore(signal.equals("Buy") ? 1 : -1);

        return result;
    }

    // TODO: Implement this method
    public List<String> getCrossAmountAndDate(String symbol, int period, LocalDateTime date, int lookback){
        return null;
    }

}
