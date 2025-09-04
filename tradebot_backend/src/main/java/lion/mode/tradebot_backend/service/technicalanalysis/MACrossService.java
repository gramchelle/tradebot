package lion.mode.tradebot_backend.service.technicalanalysis;

import java.time.LocalDateTime;

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
        result.setSignal(macd.getValue(series.getEndIndex()).doubleValue() > 0 ? "Bullish" : "Bearish");
        result.setScore(result.getSignal().equals("Bullish") ? 1 : -1);

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
        result.setSignal(macd.getValue(series.getEndIndex()).doubleValue() > 0 ? "Bullish" : "Bearish");
        result.setScore(result.getSignal().equals("Bullish") ? 1 : -1);

        return result;
    }

    public MACrossResult calculateMaCrossUntil(String symbol, int shortPeriod, int longPeriod,
                                            LocalDateTime targetDate, int lookback, String maType) {
        BarSeries series = loadSeries(symbol);
        MACrossResult result = new MACrossResult();

        if (series.getBarCount() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MA Cross for " + symbol);
        }

        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            LocalDateTime barTime = series.getBar(i).getEndTime().toLocalDateTime();
            if (!barTime.isAfter(targetDate)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1) {
            throw new IllegalArgumentException("No bar found before or at " + targetDate);
        }

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

        signal = crossover > 0 ? "Bullish" : "Bearish";

        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setMaType(maType.toUpperCase());
        result.setShortMAValue(shortMA);
        result.setLongMAValue(longMA);
        result.setCrossoverPoint(crossover);
        result.setSignal(signal);
        result.setScore(signal.equals("Bullish") ? 1 : -1);

        return result;
    }

}
