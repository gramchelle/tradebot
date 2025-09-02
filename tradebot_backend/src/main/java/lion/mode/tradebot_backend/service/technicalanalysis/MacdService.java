package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.indicators.macd.MacdResult;
import lion.mode.tradebot_backend.dto.indicators.macd.MacdResultDivergence;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class MacdService extends IndicatorService {

    public MacdService(StockDataRepository repository) {
        super(repository);
    }

    public MacdResult calculateMacd(String symbol, int shortPeriod, int longPeriod, int signalPeriod) {
        BarSeries series = loadSeries(symbol);
        
        if (series == null || series.isEmpty()) {
            throw new IllegalArgumentException("No data available for symbol: " + symbol);
        }

        MacdResult result = new MacdResult();
        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setSignalPeriod(signalPeriod);

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, shortPeriod, longPeriod);
        EMAIndicator signal = new EMAIndicator(macd, signalPeriod);

        int lastIndex = series.getEndIndex();
        result.setMacdScore(macd.getValue(lastIndex).doubleValue());
        result.setSignalScore(signal.getValue(lastIndex).doubleValue());
        result.setHistogramValue(result.getMacdScore() - result.getSignalScore());

        if (result.getHistogramValue() > 0) {
            result.setSignalText("Bullish");
            result.setScore(1);
        } else {
            result.setSignalText("Bearish");
            result.setScore(-1);
        }

        return result;
    }

    public MacdResult calculateMacdAt(String symbol, int shortPeriod, int longPeriod, int signalPeriod, LocalDateTime targetDateTime){
        BarSeries series = loadSeries(symbol);

        if (series == null || series.isEmpty()) {
            throw new IllegalArgumentException("No data available for symbol: " + symbol);
        }

        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            Bar bar = series.getBar(i);
            LocalDateTime barTime = bar.getEndTime().toLocalDateTime();

            if (!barTime.isAfter(targetDateTime)) {
                targetIndex = i;
            } else {
                break;
            }
        }

        if (targetIndex == -1) {
            throw new IllegalArgumentException("No bar found before or at " + targetDateTime);
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, shortPeriod, longPeriod);
        EMAIndicator signal = new EMAIndicator(macd, signalPeriod);

        MacdResult result = new MacdResult();
        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setSignalPeriod(signalPeriod);

        result.setMacdScore(macd.getValue(targetIndex).doubleValue());
        result.setSignalScore(signal.getValue(targetIndex).doubleValue());
        result.setHistogramValue(result.getMacdScore() - result.getSignalScore());

        if (result.getHistogramValue() > 0) {
            result.setSignalText("Bullish");
            result.setScore(1);
        } else {
            result.setSignalText("Bearish");
            result.setScore(-1);
        }

        return result;
    }

    public MacdResultDivergence calculateMacdAtWithDivergence(
            String symbol,
            int shortPeriod,
            int longPeriod,
            int signalPeriod,
            LocalDateTime targetDateTime,
            int lookback_param
    ) {
        BarSeries series = loadSeries(symbol);

        if (series == null || series.isEmpty()) {
            throw new IllegalArgumentException("No data available for symbol: " + symbol);
        }

        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            LocalDateTime barTime = series.getBar(i).getEndTime().toLocalDateTime();
            if (!barTime.isAfter(targetDateTime)) {
                targetIndex = i;
            } else break;
        }
        if (targetIndex == -1) {
            throw new IllegalArgumentException("No bar found before or at " + targetDateTime);
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, shortPeriod, longPeriod);
        EMAIndicator signal = new EMAIndicator(macd, signalPeriod);

        MacdResultDivergence result = new MacdResultDivergence();
        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setSignalPeriod(signalPeriod);

        double macdValue = macd.getValue(targetIndex).doubleValue();
        double signalValue = signal.getValue(targetIndex).doubleValue();
        double histogram = macdValue - signalValue;

        result.setMacdScore(macdValue);
        result.setSignalScore(signalValue);
        result.setHistogramValue(histogram);

        if (histogram > 0) {
            result.setSignalText("Bullish");
            result.setScore(1);
        } else {
            result.setSignalText("Bearish");
            result.setScore(-1);
        }

        int lookback = Math.min(lookback_param, targetIndex);
        double lastPrice = close.getValue(targetIndex).doubleValue();
        double prevPrice = close.getValue(targetIndex - lookback).doubleValue();
        double lastMacd = macd.getValue(targetIndex).doubleValue();
        double prevMacd = macd.getValue(targetIndex - lookback).doubleValue();
        
        if (lastPrice < prevPrice && lastMacd > prevMacd) {
            result.setDivergence("Bullish Divergence");
        } else if (lastPrice > prevPrice && lastMacd < prevMacd) {
            result.setDivergence("Bearish Divergence");
        } else {
            result.setDivergence("None");
        }

        return result;
    }

    private String detectDivergence(BarSeries series, MACDIndicator macd, int lookback) {
        int endIndex = series.getEndIndex();
        if (endIndex < lookback + 2) {
            return "None";
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);

        List<Integer> localHighs = new ArrayList<>();
        List<Integer> localLows = new ArrayList<>();

        for (int i = endIndex - lookback; i < endIndex; i++) {
            double prev = close.getValue(i - 1).doubleValue();
            double curr = close.getValue(i).doubleValue();
            double next = close.getValue(i + 1).doubleValue();

            if (curr > prev && curr > next) {
                localHighs.add(i);
            } else if (curr < prev && curr < next) {
                localLows.add(i);
            }
        }

        if (localLows.size() >= 2) {
            int lastLow = localLows.get(localLows.size() - 1);
            int prevLow = localLows.get(localLows.size() - 2);

            double priceLast = close.getValue(lastLow).doubleValue();
            double pricePrev = close.getValue(prevLow).doubleValue();

            double macdLast = macd.getValue(lastLow).doubleValue();
            double macdPrev = macd.getValue(prevLow).doubleValue();

            if (priceLast < pricePrev && macdLast > macdPrev) {
                return "Bullish Divergence";
            }
        }

        if (localHighs.size() >= 2) {
            int lastHigh = localHighs.get(localHighs.size() - 1);
            int prevHigh = localHighs.get(localHighs.size() - 2);

            double priceLast = close.getValue(lastHigh).doubleValue();
            double pricePrev = close.getValue(prevHigh).doubleValue();

            double macdLast = macd.getValue(lastHigh).doubleValue();
            double macdPrev = macd.getValue(prevHigh).doubleValue();

            if (priceLast > pricePrev && macdLast < macdPrev) {
                return "Bearish Divergence";
            }
        }

        return "None";
    }
}
