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
public class RsiService extends IndicatorService {

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

        String divergence = detectDivergence(series, period, symbol);
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
                    .timePeriod(java.time.Duration.ofHours(1))
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
            System.out.println("Fetched rows: " + dataList.size());
            throw new NotEnoughDataException("Not enough data for RSI in this interval: " + symbol);
        }

        RSIIndicator rsiIndicator = new RSIIndicator(new ClosePriceIndicator(series), period);
        double lastRsi = rsiIndicator.getValue(series.getEndIndex()).doubleValue();
        double prevRsi = rsiIndicator.getValue(series.getEndIndex() - 1).doubleValue();

        String divergence = detectDivergence(series, 90, symbol);
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

}
