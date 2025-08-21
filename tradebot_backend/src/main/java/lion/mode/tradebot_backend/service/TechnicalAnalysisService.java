package lion.mode.tradebot_backend.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import lion.mode.tradebot_backend.dto.indicators.*;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicalAnalysisService {

    private final StockDataRepository repository;

    private BarSeries loadSeries(String symbol) {

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

    public RSIResult calculateRSI(String symbol) {
        BarSeries series = loadSeries(symbol);
        RSIResult rsiResult = new RSIResult();
        rsiResult.setSymbol(symbol);

        if (series.getBarCount() < 14) { //is a rare case
            System.out.println("Not enough data to calculate RSI"); 
            return rsiResult;
        }

        RSIIndicator rsiIndicator = new RSIIndicator(new ClosePriceIndicator(series), 14);
        rsiResult.setRsiValue(rsiIndicator.getValue(series.getEndIndex()).doubleValue());

        rsiResult.setSignal(
            rsiResult.getRsiValue() > 70 ? "Sell" : // overbought; so, sell
            rsiResult.getRsiValue() < 30 ? "Buy" : "Hold"
        );

        return rsiResult;
    }

}
