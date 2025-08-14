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

import java.util.List;

@Service
public class TechnicalAnalysisService {

    public double calculateRSI(List<Double> closePrices, int period) {
        BarSeries series = buildSeries(closePrices);
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(close, period);
        return rsi.getValue(series.getEndIndex()).doubleValue();
    }

    public double[] calculateMACD(List<Double> closePrices, int shortPeriod, int longPeriod, int signalPeriod) {
        BarSeries series = buildSeries(closePrices);
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, shortPeriod, longPeriod);
        EMAIndicator signalLine = new EMAIndicator(macd, signalPeriod);
        return new double[]{
            macd.getValue(series.getEndIndex()).doubleValue(),
            signalLine.getValue(series.getEndIndex()).doubleValue()
        };
    }

    public double[] calculateBollingerBands(List<Double> closePrices, int period, double k) {
        BarSeries series = buildSeries(closePrices);
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(close, period);
        BollingerBandsMiddleIndicator middle = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsUpperIndicator upper = new BollingerBandsUpperIndicator(middle, (Indicator<Num>) close.numOf(k));
        BollingerBandsLowerIndicator lower = new BollingerBandsLowerIndicator(middle, (Indicator<Num>) close.numOf(k));
        return new double[]{
            lower.getValue(series.getEndIndex()).doubleValue(),
            middle.getValue(series.getEndIndex()).doubleValue(),
            upper.getValue(series.getEndIndex()).doubleValue()
        };
    }

    public boolean calculateMACrossover(List<Double> closePrices, int shortPeriod, int longPeriod) {
        BarSeries series = buildSeries(closePrices);
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        SMAIndicator shortMA = new SMAIndicator(close, shortPeriod);
        SMAIndicator longMA = new SMAIndicator(close, longPeriod);

        int lastIndex = series.getEndIndex();
        return shortMA.getValue(lastIndex).isGreaterThan(longMA.getValue(lastIndex)) &&
               shortMA.getValue(lastIndex - 1).isLessThan(longMA.getValue(lastIndex - 1));
    }

    public double calculateTrendlineSlope(List<Double> closePrices) {
        int n = closePrices.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += closePrices.get(i);
            sumXY += i * closePrices.get(i);
            sumX2 += i * i;
        }

        // y = ax + b -> slope (a) hesaplanıyor
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }

    private BarSeries buildSeries(List<Double> closePrices) {
        BarSeries series = new BaseBarSeriesBuilder().withName("price_series").build();
        for (Double price : closePrices) {
            series.addBar(new BaseBar(java.time.Duration.ofDays(1), null, DecimalNum.valueOf(price),
                    DecimalNum.valueOf(price), DecimalNum.valueOf(price), DecimalNum.valueOf(price),
                    DecimalNum.valueOf(0), null));
        }
        return series;
    }
}
