package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

abstract class IndicatorService {

    protected final StockDataRepository repository;

    protected IndicatorService(StockDataRepository repository) {
        this.repository = repository;
    }

    protected BarSeries loadSeries(String symbol) {

        if (repository.findBySymbol(symbol).isEmpty()) {
            throw new NotEnoughDataException("No data found for symbol: " + symbol);
        }
        
        List<StockData> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);
        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);

        for (StockData data : dataList) {
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.systemDefault());

            if (series.getBarCount() > 0) {
                ZonedDateTime lastEnd = series.getBar(series.getEndIndex()).getEndTime();
                if (!endTime.isAfter(lastEnd)) {
                    continue;
                }
            }

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
        return series;
    }

    public String detectDivergence(BarSeries series, int period, String symbol) {
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


