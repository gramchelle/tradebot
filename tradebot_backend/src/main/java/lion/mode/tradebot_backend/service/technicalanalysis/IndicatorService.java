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

abstract class IndicatorService{

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
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.of("America/New_York"));

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

    // TODO: divergence engine

}


