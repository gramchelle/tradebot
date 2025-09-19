package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.NEW_BaseIndicatorResponseDto;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.trend.UpTrendIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndexIndicator;
import org.ta4j.core.num.Num;

import java.time.Instant;

@Service
public class TestIndicatorService extends IndicatorService{

    public TestIndicatorService(StockDataRepository stockDataRepository){
        super(stockDataRepository);
    }

    /// functions for main indicator controller

    public NEW_BaseIndicatorResponseDto calculateRsi(String symbol, int period, Instant date, String source){
        symbol = symbol.toUpperCase();
        NEW_BaseIndicatorResponseDto response = new NEW_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("RSI");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = sourceSelector(source, series);

        // calculate rsi using ta4j's RSIIndicator
        RSIIndicator rsi = new RSIIndicator(prices, period);
        double rsiValue = rsi.getValue(targetIndex).doubleValue();

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("rsiValue", rsiValue);
        return response;
    }

    public NEW_BaseIndicatorResponseDto calculateBollingerBands(String symbol, int period, String basisMAtype, double stdDev, Instant date, String source){
        symbol = symbol.toUpperCase();
        NEW_BaseIndicatorResponseDto response = new NEW_BaseIndicatorResponseDto();
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

    public NEW_BaseIndicatorResponseDto calculateMACD(String symbol, int shortPeriod, int longPeriod, int signalPeriod, Instant date, String source){
        symbol = symbol.toUpperCase();
        NEW_BaseIndicatorResponseDto response = new NEW_BaseIndicatorResponseDto();
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

    public NEW_BaseIndicatorResponseDto calculateMaCrossover(String symbol, int shortPeriod, int longPeriod, String basisMAtype, Instant date, String source){
        symbol = symbol.toUpperCase();
        NEW_BaseIndicatorResponseDto response = new NEW_BaseIndicatorResponseDto();
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

    public NEW_BaseIndicatorResponseDto calculateDmi(String symbol, int period, Instant date, String source){
        symbol = symbol.toUpperCase();
        NEW_BaseIndicatorResponseDto response = new NEW_BaseIndicatorResponseDto();
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

    public NEW_BaseIndicatorResponseDto calculateMfi(String symbol, int period, Instant date){
        symbol = symbol.toUpperCase();
        NEW_BaseIndicatorResponseDto response = new NEW_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("MFI");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);

        MoneyFlowIndexIndicator moneyFlowIndexIndicator = new MoneyFlowIndexIndicator(series, period);

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("mfiValue", moneyFlowIndexIndicator.getValue(targetIndex).doubleValue());
        return response;
    }

    public NEW_BaseIndicatorResponseDto calculateTrend(String symbol, int period, Instant date){
        symbol = symbol.toUpperCase();
        NEW_BaseIndicatorResponseDto response = new NEW_BaseIndicatorResponseDto();
        response.setSymbol(symbol);
        response.setIndicator("Trend");

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);

        double closeNow  = series.getBar(targetIndex).getClosePrice().doubleValue();
        double closePrev = series.getBar(targetIndex - period).getClosePrice().doubleValue();
        double slopePerBar = (closeNow - closePrev) / (double) period;

        UpTrendIndicator  upTrendIndicator = new UpTrendIndicator(series, period);

        double isUptrend = 0.0;
        if (upTrendIndicator.getValue(targetIndex)) isUptrend = 1.0;

        response.setDate(series.getBar(targetIndex).getEndTime());
        response.getValues().put("isUptrend", isUptrend);
        response.getValues().put("slopePerBar", slopePerBar);

        return response;
    }
}
