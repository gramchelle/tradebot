package lion.mode.tradebot_backend.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

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

    // create series with DecimalNum factory
    BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);

        for (StockData data : dataList) {
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.systemDefault());

            // Skip bars that are not strictly after the last bar end time (TA4J requires strictly increasing end times)
            if (series.getBarCount() > 0) {
                ZonedDateTime lastEnd = series.getBar(series.getEndIndex()).getEndTime();
                if (!endTime.isAfter(lastEnd)) {
                    // skip duplicate or non-increasing timestamp
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
    
    public double calculateRSI(String symbol) {
        BarSeries series = loadSeries(symbol);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14); // 14 periyot RSI

        double rsiValue = rsi.getValue(series.getEndIndex()).doubleValue();

        if (rsiValue <= 30) return 1.0;   // buy signal
        if (rsiValue >= 70) return -1.0;  // sell signal
        if (rsiValue > 30 && rsiValue < 50) return (50 - rsiValue) / 20;
        if (rsiValue > 50 && rsiValue < 70) return - (rsiValue - 50) / 20;

        return rsiValue;
    }

}
