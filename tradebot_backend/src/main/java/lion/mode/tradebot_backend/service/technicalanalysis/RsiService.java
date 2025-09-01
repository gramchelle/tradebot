package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import lion.mode.tradebot_backend.dto.indicators.rsi.RSIResult;
import lion.mode.tradebot_backend.dto.indicators.rsi.RsiSeriesEntry;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RsiService extends IndicatorService {

    public RsiService(StockDataRepository repository) {
        super(repository);
    }

    /**
     * 1. Şu anki RSI (default now).
     */
    public RSIResult calculateRSI(String symbol, int period) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < period) {
            throw new NotEnoughDataException("Not enough data to calculate RSI for " + symbol);
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(close, period);

        int lastIndex = series.getEndIndex();
        double lastRsi = rsi.getValue(lastIndex).doubleValue();

        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setRsiValue(lastRsi);
        result.setDate(series.getLastBar().getEndTime().toLocalDateTime());
        result.setSignal(generateSignal(lastRsi));
        result.setTrendComment("Latest RSI is " + lastRsi);

        return result;
    }

    /**
     * 2. Belirli tarihte RSI hesapla.
     */
    public RSIResult calculateRSI(String symbol, int period, LocalDateTime targetDate) {
        List<StockData> dataList = repository
                .findBySymbolAndTimestampLessThanEqualOrderByTimestampAsc(symbol, targetDate);

        if (dataList.size() < period + 1) {
            throw new NotEnoughDataException("Not enough data for RSI at " + targetDate + " for " + symbol);
        }

        Duration barDuration = detectBarDuration(dataList);

        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);
        for (StockData data : dataList) {
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.systemDefault());
            Bar bar = BaseBar.builder()
                    .timePeriod(barDuration)
                    .endTime(endTime)
                    .openPrice(DecimalNum.valueOf(data.getOpen()))
                    .highPrice(DecimalNum.valueOf(data.getHigh()))
                    .lowPrice(DecimalNum.valueOf(data.getLow()))
                    .closePrice(DecimalNum.valueOf(data.getClose()))
                    .volume(DecimalNum.valueOf(data.getVolume()))
                    .build();
            series.addBar(bar);
        }

        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            if (series.getBar(i).getEndTime().toLocalDateTime().equals(targetDate)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found at " + targetDate + " for " + symbol);
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(close, period);
        double rsiValue = rsi.getValue(targetIndex).doubleValue();

        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setRsiValue(rsiValue);
        result.setDate(targetDate);
        result.setSignal(generateSignal(rsiValue));
        result.setTrendComment("RSI at " + targetDate + " is " + rsiValue);

        return result;
    }

    /**
     * 3. Şimdiki RSI + Divergence.
     */
    public RSIResult calculateRSIWithDivergence(String symbol, int period) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < period + 3) {
            throw new NotEnoughDataException("Not enough data to calculate RSI + divergence for " + symbol);
        }

        RSIResult rsiResult = calculateRSI(symbol, period);

        String divergence = detectDivergence(series, period, symbol);
        rsiResult.setDivergence(divergence);

        return rsiResult;
    }

    /**
     * 4. Belirli tarihte RSI + Divergence.
     */
    public RSIResult calculateRSIWithDivergence(String symbol, int period, LocalDateTime targetDate) {
        List<StockData> dataList = repository
                .findBySymbolAndTimestampLessThanEqualOrderByTimestampAsc(symbol, targetDate);

        if (dataList.size() < period + 3) {
            throw new NotEnoughDataException("Not enough data for RSI divergence at " + targetDate + " for " + symbol);
        }

        Duration barDuration = detectBarDuration(dataList);

        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);
        for (StockData data : dataList) {
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.systemDefault());
            Bar bar = BaseBar.builder()
                    .timePeriod(barDuration)
                    .endTime(endTime)
                    .openPrice(DecimalNum.valueOf(data.getOpen()))
                    .highPrice(DecimalNum.valueOf(data.getHigh()))
                    .lowPrice(DecimalNum.valueOf(data.getLow()))
                    .closePrice(DecimalNum.valueOf(data.getClose()))
                    .volume(DecimalNum.valueOf(data.getVolume()))
                    .build();
            series.addBar(bar);
        }

        // targetIndex bul
        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            if (series.getBar(i).getEndTime().toLocalDateTime().equals(targetDate)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found at " + targetDate + " for " + symbol);
        }

        // RSI hesapla
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(close, period);
        double rsiValue = rsi.getValue(targetIndex).doubleValue();

        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setRsiValue(rsiValue);
        result.setDate(targetDate);
        result.setSignal(generateSignal(rsiValue));
        result.setTrendComment("RSI with divergence at " + targetDate + " is " + rsiValue);

        // divergence hesapla
        String divergence = detectDivergence(series, period, symbol);
        result.setDivergence(divergence);

        return result;
    }

    // helpers
    private Duration detectBarDuration(List<StockData> dataList) {
        if (dataList.size() < 2) return Duration.ofMinutes(30);
        LocalDateTime t0 = dataList.get(0).getTimestamp();
        LocalDateTime t1 = dataList.get(1).getTimestamp();
        return Duration.between(t0, t1);
    }

    private String generateSignal(double rsiValue) {
        if (rsiValue > 70) return "Sell";
        if (rsiValue < 30) return "Buy";
        return "Hold";
    }

    public String detectDivergence(BarSeries series, int period, String symbol) {    
        // require a bit more history to find local extrema
        if (series.getBarCount() < period + 3) {
            throw new NotEnoughDataException("NOT ENOUGH DATA FOR " + symbol);
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, period);

        int endIndex = series.getEndIndex();
        // lookback window: a few periods to find meaningful local highs/lows
        int lookback = Math.min(period * 3, endIndex);
        int startIndex = Math.max(1, endIndex - lookback); // ensure i-1 exists

        List<Integer> lowIndices = new ArrayList<>();
        List<Integer> highIndices = new ArrayList<>();

        // collect local lows/highs within the lookback window
        for (int i = startIndex; i <= endIndex - 1; i++) {
            double price = closePrice.getValue(i).doubleValue();
            double prevPrice = closePrice.getValue(i - 1).doubleValue();
            double nextPrice = closePrice.getValue(i + 1).doubleValue();

            if (price < prevPrice && price < nextPrice) {
                lowIndices.add(i);
            }
            if (price > prevPrice && price > nextPrice) {
                highIndices.add(i);
            }
        }

        // Regular bullish divergence:
        // price makes lower low but RSI makes higher low (last two lows)
        if (lowIndices.size() >= 2) {
            int i1 = lowIndices.get(lowIndices.size() - 2);
            int i2 = lowIndices.get(lowIndices.size() - 1);
            double p1 = closePrice.getValue(i1).doubleValue();
            double p2 = closePrice.getValue(i2).doubleValue();
            double r1 = rsi.getValue(i1).doubleValue();
            double r2 = rsi.getValue(i2).doubleValue();

            if (p2 < p1 && r2 > r1) {
                return "bullish";
            }
        }

        // Regular bearish divergence:
        // price makes higher high but RSI makes lower high (last two highs)
        if (highIndices.size() >= 2) {
            int i1 = highIndices.get(highIndices.size() - 2);
            int i2 = highIndices.get(highIndices.size() - 1);
            double p1 = closePrice.getValue(i1).doubleValue();
            double p2 = closePrice.getValue(i2).doubleValue();
            double r1 = rsi.getValue(i1).doubleValue();
            double r2 = rsi.getValue(i2).doubleValue();

            if (p2 > p1 && r2 < r1) {
                return "bearish";
            }
        }

        // no regular divergence found
        return "none";
    }

}