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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public abstract class IndicatorService{

    protected final StockDataRepository repository;

    protected IndicatorService(StockDataRepository repository) {
        this.repository = repository;
    }

    // TODO: Make this method generic to support different timeframes
    protected BarSeries loadSeries(String symbol) {

        if (repository.findBySymbol(symbol).isEmpty()) {
            throw new NotEnoughDataException("No data found for symbol: " + symbol);
        }
        
        List<StockDataDaily> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);
        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);

        for (StockDataDaily data : dataList) {
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.systemDefault());
            if (series.getBarCount() > 0) {
                ZonedDateTime lastEnd = series.getBar(series.getEndIndex()).getEndTime();
                if (!endTime.isAfter(lastEnd)) {
                    continue;
                }
            }

            Bar bar = BaseBar.builder()
                    .timePeriod(java.time.Duration.ofDays(1))
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

    protected int seriesAmountValidator(String symbol, BarSeries series, LocalDateTime date){
        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            LocalDateTime barTime = series.getBar(i).getEndTime().toLocalDateTime();
            if (!barTime.isAfter(date)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found before or at " + date + " for " + symbol);
        }
        
        return targetIndex;
    }

    protected Indicator<Num> sourceSelector(String priceType, BarSeries series){
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


