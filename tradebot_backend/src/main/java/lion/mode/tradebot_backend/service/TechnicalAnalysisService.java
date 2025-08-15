package lion.mode.tradebot_backend.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import lion.mode.tradebot_backend.dto.indicators.BollingerResult;
import lion.mode.tradebot_backend.dto.indicators.MACrossoverResult;
import lion.mode.tradebot_backend.dto.indicators.MacdResult;
import lion.mode.tradebot_backend.dto.indicators.TrendlineResult;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicalAnalysisService {

    private final StockDataRepository repository;

    // BarSeries, veritabanındaki ham stock_data'nın ta4j teknik analiz kütüphanesinin anlayıp kullanabileceği bir formattır
    private BarSeries loadSeries(String symbol) {

        //fetches all stock data by symbol, OrderByTimestampAsc teknik analiz için şarttır, bu nedenle verileri sıralar
        List<StockData> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);

    // DecimalNum factory, finansal hesaplamalardaki küçük ondalık hatalarını önlemeye yarayan yüksek hassasiyetli bir sayı formatı kullanılmasını sağlar
    BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);

        // this for loop takes OHLCV values from StockData entity, converts and assigns it into Bar object
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

    // Seri, ta4j kütüphanesi formatına çevirilerek, BarSeries nesnesi döndürülür.
    return series;
    }

    public double calculateRSI(String symbol) {
        BarSeries series = loadSeries(symbol);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14); // timeframe/period to be used in the AVG GAINS and AVG LOSSES calculation

        double rsiValue = rsi.getValue(series.getEndIndex()).doubleValue();

        if (rsiValue <= 30) return 1.0;   // buy signal
        if (rsiValue >= 70) return -1.0;  // sell signal
        if (rsiValue > 30 && rsiValue < 50) return (50 - rsiValue) / 20; // 30 and 70 limits are selected by default, can be changed thoroughly
        if (rsiValue > 50 && rsiValue < 70) return - (rsiValue - 50) / 20;

        return rsiValue;
    }

    public MacdResult calculateMACD(String symbol) {
        // MACD, bir hisse senedinin momentumunu ve trendinin yönünü ölçen popüler bir teknik analiz göstergesidir.
        BarSeries series = loadSeries(symbol);
        if (series.getBarCount() == 0) return new MacdResult();

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, 12, 26); // Using close prices, calculates MACD line by taking differences of standard 12 and 26 period EMAs.
        EMAIndicator signal = new EMAIndicator(macd, 9); // MACD çizgisinin kendisinin 9 periyotluk EMA'sını alarak "sinyal çizgisi"ni oluşturuyor.

        int idx = series.getEndIndex();
        Num macdVal = macd.getValue(idx);
        Num signalVal = signal.getValue(idx);
        Num hist = macdVal.minus(signalVal);

        String signalStr = hist.isGreaterThan(DecimalNum.valueOf(0)) ? "buy" : hist.isLessThan(DecimalNum.valueOf(0)) ? "sell" : "hold";

        // gerçek "buy" sinyali, MACD çizgisi, sinyal çizgisini aşağıdan yukarıya doğru kestiğinde oluşur. Bu momentumun negatife dönmekte olduğunun güçlü bir işaretidir.
        // gerçek "sell" sinyali, MACD çizgisi, sinyal çizgisini yukarıdan aşağı doğru kestiğinde oluşur.

        MacdResult macdResult = new MacdResult();
        macdResult.setSymbol(symbol);
        macdResult.setMacd(macdVal.doubleValue());
        macdResult.setSignal(signalVal.doubleValue());
        macdResult.setHistogram(hist.doubleValue());
        macdResult.setTradeSignal(signalStr);

        return macdResult;
    }

    public MACrossoverResult calculateMACrossover(String symbol) {
        BarSeries series = loadSeries(symbol);
        if (series.getBarCount() < 2) return new MACrossoverResult();

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        EMAIndicator shortEma = new EMAIndicator(close, 12); //kısa vadeli (hızlı) EMA'yı hesaplar
        EMAIndicator longEma = new EMAIndicator(close, 26); // uzun vadeli (yavaş) EMA'yı hesaplar

        int last = series.getEndIndex();
        int prev = Math.max(0, last - 1);

        Num shortNow = shortEma.getValue(last); // şimdiki zamanın kısa vadeli ortalamalarının değerlerini al
        Num longNow = longEma.getValue(last); // şimdiki zamanın uzun vadeli ortalamalarının değerlerini al
        Num shortPrev = shortEma.getValue(prev); // bir önceki zamandaki kısa vadeli ortalamaların değerlerini al
        Num longPrev = longEma.getValue(prev); // bir önceki zamandaki uzun vadeli ortalamaların değerlerini al

        String signal;
        if (shortNow.isGreaterThan(longNow) && shortPrev.isLessThanOrEqual(longPrev)) signal = "buy"; // Golden Cross -> Eğer şimdi kısa ortalama, uzun ortalamanın üstündeyse ve bir önce kısa ortalama, uzun ortalamanın altında ya da eşit idiyse, bu, kısa ortalamanın uzun ortalamayı yukarı doğru kestiği anlamına gelir.
        else if (shortNow.isLessThan(longNow) && shortPrev.isGreaterThanOrEqual(longPrev)) signal = "sell"; // Death Cross -> Eğer şimdi kısa ortalama, uzun ortalamanın altındaysa ve bir önce kısa ortalama, uzun ortalamanın üstünde ya da eşit idiyse, bu, kısa ortalamanın uzun ortalamayı aşağı doğru kestiği anlamına gelir.
        else signal = "hold";

        MACrossoverResult result = new MACrossoverResult();
        result.setSymbol(symbol);
        result.setShortEma(shortNow.doubleValue());
        result.setLongEma(longNow.doubleValue());
        result.setSignal(signal);

        return result;
    }

    public BollingerResult calculateBollinger(String symbol) {
        BarSeries series = loadSeries(symbol);
        if (series.getBarCount() == 0) return new BollingerResult();

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        int period = 20;
        SMAIndicator sma = new SMAIndicator(close, period);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(close, period);

        int idx = series.getEndIndex();
        Num middle = sma.getValue(idx);
        Num deviation = sd.getValue(idx);
        Num upper = middle.plus(deviation.multipliedBy(DecimalNum.valueOf(2)));
        Num lower = middle.minus(deviation.multipliedBy(DecimalNum.valueOf(2)));

        Num lastClose = close.getValue(idx);
        String signal = lastClose.isLessThan(lower) ? "buy" : lastClose.isGreaterThan(upper) ? "sell" : "hold";

        BollingerResult result = new BollingerResult();
        result.setSymbol(symbol);
        result.setUpperBand(upper.doubleValue());
        result.setMiddleBand(middle.doubleValue());
        result.setLowerBand(lower.doubleValue());
        result.setClosePrice(lastClose.doubleValue());
        result.setSignal(signal);

        return result;
    }

    public TrendlineResult calculateTrend(String symbol) {
        // Trendline is calculated to see the relationship between last close and past close.
        BarSeries series = loadSeries(symbol);
        int period = 20;
        if (series.getBarCount() < period) return new TrendlineResult();

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        int last = series.getEndIndex();
        int past = Math.max(0, last - period + 1);

        Num lastClose = close.getValue(last);
        Num pastClose = close.getValue(past);

        double slope = (lastClose.doubleValue() - pastClose.doubleValue()) / period;

        String trend = slope > 0 ? "uptrend" : slope < 0 ? "downtrend" : "sideways";

        TrendlineResult result = new TrendlineResult();
        result.setSymbol(symbol);
        result.setSlope(slope);
        result.setLastClose(lastClose.doubleValue());
        result.setPastClose(pastClose.doubleValue());
        result.setTrend(trend);

        return result;
    }

}
