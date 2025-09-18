package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.BollingerBandsEntry;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.*;

@Service
public class BollingerBandsService extends IndicatorService {

    public BollingerBandsService(StockDataRepository repository) {
        super(repository);
    }

    public BaseIndicatorResponse calculate(BollingerBandsEntry entry){
        BarSeries series = loadSeries(entry.getSymbol().toUpperCase());
        return calculateWithSeries(entry, series);
    }

    public BaseIndicatorResponse calculateWithSeries(BollingerBandsEntry entry, BarSeries series){
        String symbol = entry.getSymbol().toUpperCase();
        double nbDev = entry.getNumberOfDeviations();
        int period = entry.getPeriod();
        Instant date = entry.getDate();
        String source = entry.getSource();
        double squeezeConfidence = entry.getSqueezeConfidence();
                
        if (series.getBarCount() < period) throw new NotEnoughDataException("Not enough data for Bollinger Bands for symbol: " + symbol);

        int targetIndex = seriesAmountValidator(symbol, series, date);
        
        if (targetIndex < period - 1) throw new NotEnoughDataException("Target index " + targetIndex + " is insufficient for period " + period + " for " + symbol);

        Indicator<Num> prices = sourceSelector(source, series);
        SMAIndicator sma = new SMAIndicator(prices, period);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(prices, period);

        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd, DecimalNum.valueOf(nbDev));
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd, DecimalNum.valueOf(nbDev));

        double middle = bbm.getValue(targetIndex).doubleValue();
        double upper = bbu.getValue(targetIndex).doubleValue();
        double lower = bbl.getValue(targetIndex).doubleValue();
        double currentPrice = prices.getValue(targetIndex).doubleValue();
        
        double bandwidth = 0;
        if (middle != 0) {
            bandwidth = ((upper - lower) / middle) * 100;
        }
        
        double percentB = 0;
        if ((upper - lower) != 0) {
            percentB = (currentPrice - lower) / (upper - lower);
        }

        BaseIndicatorResponse result = new BaseIndicatorResponse();
        result.setSymbol(symbol);
        result.setDate(series.getBar(targetIndex).getEndTime());
        result.setIndicator("Bollinger Bands");
        result.getValues().put("%B", percentB);
        result.getValues().put("lowerBand", lower);
        result.getValues().put("middleBand", middle);
        result.getValues().put("upperBand", upper);
        result.getValues().put("bandwidth", bandwidth);
        result.getValues().put("currentPrice", currentPrice);

        boolean squeeze = bandwidth < squeezeConfidence;
        result.setStatus(squeeze ? "Squeeze Detected (Low Volatility)" : "Normal Volatility");

        generateBollingerSignal(result, percentB, currentPrice, middle, bandwidth, squeeze);
        int barsSinceSignal = calculateBarsSinceSignal(bbu, bbl, prices, targetIndex);
        result.setBarsSinceSignal(barsSinceSignal);

        return result;
    }

    private void generateBollingerSignal(BaseIndicatorResponse result, double percentB, double currentPrice, double middle, double bandwidth, boolean squeeze) {
        if (squeeze) {
            result.setSignal("Hold - Awaiting Breakout");
            result.setScore(0.0);
            return;
        }
        
        if (percentB > 1.0) {
            if (percentB > 1.2) {
                result.setSignal("Strong Sell");
                result.setScore(-3.0/3.0);
            } else {
                result.setSignal("Sell");
                result.setScore(-2.0/3.0);
            }
        } else if (percentB < 0.0) {
            if (percentB < -0.2) {
                result.setSignal("Strong Buy");
                result.setScore(1.0/3.0);
            } else {
                result.setSignal("Buy");
                result.setScore(2.0/3.0);
            }
        } else {
            if (percentB > 0.8) {
                result.setSignal("Weak Sell");
                result.setScore(-1.0/3.0);
            } else if (percentB < 0.2) {
                result.setSignal("Weak Buy");
                result.setScore(1.0/3.0);
            } else {
                boolean bullish = currentPrice > middle;
                result.setSignal(bullish ? "Hold - Bullish" : "Hold - Bearish");
                result.setScore(0.0);
            }
        }
    }

    private int calculateBarsSinceSignal(BollingerBandsUpperIndicator upperBand, BollingerBandsLowerIndicator lowerBand, Indicator<Num> prices, int targetIndex) {
        if (targetIndex <= 0) {
            return -1;
        }
        
        double currentPrice = prices.getValue(targetIndex).doubleValue();
        double currentUpper = upperBand.getValue(targetIndex).doubleValue();
        double currentLower = lowerBand.getValue(targetIndex).doubleValue();
        
        boolean currentAboveUpper = currentPrice > currentUpper;
        boolean currentBelowLower = currentPrice < currentLower;
        
        if (!currentAboveUpper && !currentBelowLower) {
            return 0;
        }
        
        for (int i = targetIndex - 1; i >= 0; i--) {
            double prevPrice = prices.getValue(i).doubleValue();
            double prevUpper = upperBand.getValue(i).doubleValue();
            double prevLower = lowerBand.getValue(i).doubleValue();
            
            if (currentAboveUpper) {
                if (prevPrice <= prevUpper) {
                    return targetIndex - i;
                }
            } else if (currentBelowLower) {
                if (prevPrice >= prevLower) {
                    return targetIndex - i;
                }
            }
        }
        
        return 0;
    }

}
