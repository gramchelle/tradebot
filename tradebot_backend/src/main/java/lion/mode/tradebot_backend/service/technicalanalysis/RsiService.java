package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import lion.mode.tradebot_backend.dto.indicators.*;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class RsiService extends AbstractIndicatorService {

    public RsiService(StockDataRepository repository) {
        super(repository);
    }

    public RSIResult calculateRSI(String symbol, int period) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < period) {
            throw new NotEnoughDataException("Not enough data to calculate RSI for " + symbol);
        }

        RSIIndicator rsiIndicator = new RSIIndicator(new ClosePriceIndicator(series), period);
        double lastRsi = rsiIndicator.getValue(series.getEndIndex()).doubleValue();
        double prevRsi = rsiIndicator.getValue(series.getEndIndex() - 1).doubleValue();

        RSIResult rsiResult = new RSIResult();
        rsiResult.setSymbol(symbol);
        rsiResult.setPeriod(period);
        rsiResult.setRsiValue(lastRsi);

        String divergence = detectRsiDivergence(series, period, symbol);
        rsiResult.setDivergence(divergence);

        if (lastRsi > 70 && lastRsi < prevRsi) {
            rsiResult.setSignal("Sell");
            rsiResult.setScore(-1);
        } else if (lastRsi < 30 && lastRsi > prevRsi) {
            rsiResult.setSignal("Buy");
            rsiResult.setScore(1);
        } else {
            rsiResult.setSignal("Hold");
            rsiResult.setScore(0);
        }

        return rsiResult;
    }

    public RSIResult calculateRSI(String symbol, int period, LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        List<StockData> dataList = repository.findBySymbolAndTimestampBetweenOrderByTimestampAsc(
                symbol, startDateTime, endDateTime
        );

        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);

        for (StockData data : dataList) {
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.systemDefault());
            Bar bar = BaseBar.builder()
                    .timePeriod(java.time.Duration.ofMinutes(1))
                    .endTime(endTime)
                    .openPrice(DecimalNum.valueOf(data.getOpen()))
                    .highPrice(DecimalNum.valueOf(data.getHigh()))
                    .lowPrice(DecimalNum.valueOf(data.getLow()))
                    .closePrice(DecimalNum.valueOf(data.getClose()))
                    .volume(DecimalNum.valueOf(data.getVolume()))
                    .build();
            series.addBar(bar);
        }

        RSIResult rsiResult = new RSIResult();
        rsiResult.setSymbol(symbol);
        rsiResult.setPeriod(period);

        if (series.getBarCount() < period) {
            throw new NotEnoughDataException("Not enough data for RSI in this interval: " + symbol);
        }

        RSIIndicator rsiIndicator = new RSIIndicator(new ClosePriceIndicator(series), period);
        double lastRsi = rsiIndicator.getValue(series.getEndIndex()).doubleValue();
        double prevRsi = rsiIndicator.getValue(series.getEndIndex() - 1).doubleValue();

        String divergence = detectRsiDivergence(series, period, symbol);
        rsiResult.setDivergence(divergence);

        rsiResult.setRsiValue(lastRsi);
        if (lastRsi < 30 && lastRsi > prevRsi) {
            rsiResult.setSignal("Buy");
            rsiResult.setScore(+1);
        } else if (lastRsi > 70 && lastRsi < prevRsi) {
            rsiResult.setSignal("Sell");
            rsiResult.setScore(-1);
        } else {
            rsiResult.setSignal("Hold");
            rsiResult.setScore(0);
        }

        return rsiResult;
    }

    public String detectRsiDivergence(BarSeries series, int period, String symbol) {
        if (series.getBarCount() < period + 2) {
            throw new NotEnoughDataException("NOT ENOUGH DATA FOR " + symbol);
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, period);

        int endIndex = series.getEndIndex();
        int lookback = Math.min(period, endIndex);

        Double lastPriceLow = null, lastRsiLow = null;
        Double lastPriceHigh = null, lastRsiHigh = null;

        for (int i = endIndex - lookback; i < endIndex - 1; i++) {
            if (i <= 0) continue;
            if (i >= endIndex - 1) break;

            double price = closePrice.getValue(i).doubleValue();
            double prevPrice = closePrice.getValue(i - 1).doubleValue();
            double nextPrice = closePrice.getValue(i + 1).doubleValue();

            double rsiVal = rsi.getValue(i).doubleValue();
            double prevRsi = rsi.getValue(i - 1).doubleValue();
            double nextRsi = rsi.getValue(i + 1).doubleValue();

            // local low
            if (price < prevPrice && price < nextPrice) {
                if (lastPriceLow != null && lastRsiLow != null) {
                    if (price < lastPriceLow && rsiVal > lastRsiLow) {
                        System.out.println("Bullish divergence detected at index " + i);
                        return "bullish";
                    }
                }
                lastPriceLow = price;
                lastRsiLow = rsiVal;
            }

            // local high
            if (price > prevPrice && price > nextPrice) {
                if (lastPriceHigh != null && lastRsiHigh != null) {
                    if (price > lastPriceHigh && rsiVal < lastRsiHigh) {
                        System.out.println("Bearish divergence detected at index " + i);
                        return "bearish";
                    }
                }
                lastPriceHigh = price;
                lastRsiHigh = rsiVal;
            }
        }

        return "none";
    }
}
