package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.*;
import java.util.List;

public abstract class IndicatorService {

    protected final StockDataRepository repository;

    protected IndicatorService(StockDataRepository repository) {
        this.repository = repository;
    }

    protected BarSeries loadSeries(String symbol) {
        if (repository.findBySymbol(symbol).isEmpty()) {
            throw new NotEnoughDataException("No data found for symbol: " + symbol);
        }

        List<StockDataDaily> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);
        BarSeries series = new BaseBarSeriesBuilder()
                .withName(symbol)
                .build();

        for (StockDataDaily data : dataList) {
            Instant endTimeInstant = data.getTimestamp().atZone(ZoneId.systemDefault()).toInstant();

            if (series.getBarCount() > 0) {
                Instant lastEnd = series.getBar(series.getEndIndex()).getEndTime();
                if (!endTimeInstant.isAfter(lastEnd)) {
                    continue;
                }
            }

            Bar bar = new BaseBar(
                    Duration.ofDays(1),
                    endTimeInstant,
                    DecimalNum.valueOf(data.getOpen()),
                    DecimalNum.valueOf(data.getHigh()),
                    DecimalNum.valueOf(data.getLow()),
                    DecimalNum.valueOf(data.getClose()),
                    DecimalNum.valueOf(data.getVolume()),
                    DecimalNum.valueOf(0),
                    0
            );

            series.addBar(bar);
        }

        return series;
    }

    protected int seriesAmountValidator(String symbol, BarSeries series, Instant targetInstant) {
        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            Instant barEnd = series.getBar(i).getEndTime();
            if (!barEnd.isAfter(targetInstant)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found before or at " + targetInstant + " for " + symbol);
        }

        return targetIndex;
    }

    protected Indicator<Num> sourceSelector(String priceType, BarSeries series) {
        switch (priceType.toLowerCase()) {
            case "open":
                return new OpenPriceIndicator(series);
            case "close":
                return new ClosePriceIndicator(series);
            case "high":
                return new HighPriceIndicator(series);
            case "low":
                return new LowPriceIndicator(series);
            default:
                throw new IllegalArgumentException("Invalid price type: " + priceType);
        }
    }
}
