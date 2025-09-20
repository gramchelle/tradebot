package lion.mode.tradebot_backend.service.technicalanalysis.N_technicalanalysis;

import lion.mode.tradebot_backend.dto.base_responses.N_BaseIndicatorResponseDto;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.trend.DownTrendIndicator;
import org.ta4j.core.indicators.trend.UpTrendIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndexIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class N_IndicatorService {

    private final StockDataRepository repository;

    /// Main Indicator Controller Functions
    public N_BaseIndicatorResponseDto calculateRsi(String symbol, int period, Instant date, String source){
        symbol = symbol.toUpperCase();
        N_BaseIndicatorResponseDto response = new N_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("RSI");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = sourceSelector(source, series);

        RSIIndicator rsi = new RSIIndicator(prices, period);
        double rsiValue = rsi.getValue(targetIndex).doubleValue();

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("rsiValue", rsiValue);
        return response;
    }

    public N_BaseIndicatorResponseDto calculateBollingerBands(String symbol, int period, String basisMAtype, double stdDev, Instant date, String source){
        symbol = symbol.toUpperCase();
        N_BaseIndicatorResponseDto response = new N_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("Bollinger Bands");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = sourceSelector(source, series);

        BollingerBandFacade bollingerBandFacade = new BollingerBandFacade(series, period, stdDev);

        NumericIndicator upper = bollingerBandFacade.upper();

        Indicator<Num> middle;
        if (basisMAtype.isEmpty() || basisMAtype.equalsIgnoreCase("sma")) {
            middle = bollingerBandFacade.middle();
        } else if (basisMAtype.equalsIgnoreCase("ema")) {
            middle = new EMAIndicator(prices, period);
        } else {
            throw new IllegalArgumentException("Invalid MA type: " + basisMAtype);
        }

        NumericIndicator lower = bollingerBandFacade.lower();
        NumericIndicator percentB = bollingerBandFacade.percentB();
        NumericIndicator bandwidth = bollingerBandFacade.bandwidth();

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("upper", upper.getValue(targetIndex).doubleValue());
        response.getValues().put("middle", middle.getValue(targetIndex).doubleValue());
        response.getValues().put("lower", lower.getValue(targetIndex).doubleValue());
        response.getValues().put("percentB", percentB.getValue(targetIndex).doubleValue());
        response.getValues().put("bandwidth",  bandwidth.getValue(targetIndex).doubleValue());

        return response;
    }

    public N_BaseIndicatorResponseDto calculateMACD(String symbol, int shortPeriod, int longPeriod, int signalPeriod, Instant date, String source){
        symbol = symbol.toUpperCase();
        N_BaseIndicatorResponseDto response = new N_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("MACD");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = sourceSelector(source, series);

        MACDIndicator macdIndicator = new MACDIndicator(prices, shortPeriod, longPeriod);

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("macdLine", (macdIndicator.getShortTermEma().getValue(targetIndex).doubleValue()) - macdIndicator.getLongTermEma().getValue(targetIndex).doubleValue());
        response.getValues().put("signalLine",  macdIndicator.getSignalLine(signalPeriod).getValue(targetIndex).doubleValue());
        response.getValues().put("histogram", macdIndicator.getHistogram(signalPeriod).getValue(targetIndex).doubleValue());

        return response;
    }

    public N_BaseIndicatorResponseDto calculateMaCrossover(String symbol, int shortPeriod, int longPeriod, String basisMAtype, Instant date, String source){
        symbol = symbol.toUpperCase();
        N_BaseIndicatorResponseDto response = new N_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("MACrossover");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = sourceSelector(source, series);

        CachedIndicator<Num> shortMA = null, longMA = null;

        if (basisMAtype.equalsIgnoreCase("sma")){
            shortMA =  new SMAIndicator(prices, shortPeriod);
            longMA = new SMAIndicator(prices, longPeriod);
        } else if (basisMAtype.equalsIgnoreCase("ema")){
            shortMA = new EMAIndicator(prices, shortPeriod);
            longMA = new EMAIndicator(prices, longPeriod);
        }

        if (shortMA == null || longMA == null) {
            throw new IllegalArgumentException("Invalid MA type: " + basisMAtype);
        }

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("shortMA", shortMA.getValue(targetIndex).doubleValue());
        response.getValues().put("longMA", longMA.getValue(targetIndex).doubleValue());
        return response;
    }

    public N_BaseIndicatorResponseDto calculateDmi(String symbol, int period, Instant date, String source){
        symbol = symbol.toUpperCase();
        N_BaseIndicatorResponseDto response = new N_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("DMI");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = sourceSelector(source, series);

        PlusDIIndicator plusDIIndicator = new PlusDIIndicator(series, period);
        MinusDIIndicator minusDIIndicator = new MinusDIIndicator(series, period);
        ADXIndicator adxIndicator = new ADXIndicator(series, period);

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("adxValue", adxIndicator.getValue(targetIndex).doubleValue());
        response.getValues().put("plusDIValue", plusDIIndicator.getValue(targetIndex).doubleValue());
        response.getValues().put("minusDIValue", minusDIIndicator.getValue(targetIndex).doubleValue());

        return response;
    }

    public N_BaseIndicatorResponseDto calculateMfi(String symbol, int period, Instant date){
        symbol = symbol.toUpperCase();
        N_BaseIndicatorResponseDto response = new N_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("MFI");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);

        MoneyFlowIndexIndicator moneyFlowIndexIndicator = new MoneyFlowIndexIndicator(series, period);

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("mfiValue", moneyFlowIndexIndicator.getValue(targetIndex).doubleValue());
        return response;
    }

    public N_BaseIndicatorResponseDto calculateTrend(String symbol, int period, Instant date) {
        symbol = symbol.toUpperCase();
        N_BaseIndicatorResponseDto response = new N_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("Trend");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);

        double closeNow = series.getBar(targetIndex).getClosePrice().doubleValue();
        double closePrev = series.getBar(targetIndex - period).getClosePrice().doubleValue();
        double slopePerBar = ((closeNow - closePrev) / (double) period) * 100;

        UpTrendIndicator upTrendIndicator = new UpTrendIndicator(series, period);
        DownTrendIndicator downTrendIndicator = new DownTrendIndicator(series, period);

        // Yukarı trend varsa 1, aşağı trend varsa -1, trend yoksa 0
        double trend;
        if (upTrendIndicator.getValue(targetIndex)) {
            trend = 1.0;
        } else if (downTrendIndicator.getValue(targetIndex)) {
            trend = -1.0;
        } else {
            trend = 0.0;
        }

        // Swing High / Low hesaplama -> eğer bir barın iki tarafındaki beşer bar da ondan yüksek bir seviyedeyse, bu swing low olur.
        RecentSwingHighIndicator swingHighIndicator = new RecentSwingHighIndicator(series, 5);
        RecentSwingLowIndicator swingLowIndicator = new RecentSwingLowIndicator(series, 5);

        Num recentHigh = swingHighIndicator.getValue(targetIndex);
        Num recentLow = swingLowIndicator.getValue(targetIndex);

        double swingHigh = recentHigh.isNaN() ? Double.NaN : recentHigh.doubleValue();
        double swingLow = recentLow.isNaN() ? Double.NaN : recentLow.doubleValue();

        List<Double> supportLevels = new ArrayList<>();
        List<Double> resistanceLevels = new ArrayList<>();

        for (int i = series.getBeginIndex(); i <= targetIndex; i++) {
            Num high = swingHighIndicator.getValue(i);
            Num low = swingLowIndicator.getValue(i);

            if (!high.isNaN()) resistanceLevels.add(high.doubleValue());
            if (!low.isNaN()) supportLevels.add(low.doubleValue());
        }

        // en yakın destek ve direnç: en sık swing high ve low'lara göre belirlenir
        double nearestSupport = supportLevels.isEmpty() ? Double.NaN : supportLevels.get(supportLevels.size() - 1);
        double nearestResistance = resistanceLevels.isEmpty() ? Double.NaN : resistanceLevels.get(resistanceLevels.size() - 1);

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("isUptrend", trend); //-1 for downtrend, 1 for uptrend, 0 for no obvious trend
        response.getValues().put("currentPrice", closeNow);
        response.getValues().put("slopeAtDate", slopePerBar);
        response.getValues().put("recentSwingHigh", swingHigh);
        response.getValues().put("recentSwingLow", swingLow);
        response.getValues().put("nearestSupport", nearestSupport);
        response.getValues().put("nearestResistance", nearestResistance);

        return response;
    }

    // Helpers

    protected BarSeries loadSeries(String symbol) {

        if (repository.findBySymbol(symbol).isEmpty()) {
            throw new NotEnoughDataException("No data found for symbol: " + symbol);
        }

        List<StockDataDaily> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);
        BarSeries series = new BaseBarSeriesBuilder()
                .withName(symbol)
                .build();


        for (StockDataDaily data : dataList) {
            LocalDateTime localEndTime = data.getTimestamp();
            ZonedDateTime zonedEndTime = localEndTime.atZone(ZoneId.systemDefault());
            Instant endTimeInstant = zonedEndTime.toInstant();

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

    protected int seriesAmountValidator(String symbol, BarSeries series, Instant date) {
        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            Instant barEndTime = series.getBar(i).getEndTime();
            if (!barEndTime.isAfter(date)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found before or at " + date + " for " + symbol);
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
