package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.TrendlineEntry;
import lion.mode.tradebot_backend.repository.StockDataRepository;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.time.LocalDateTime;

@Service
public class TrendlineService extends IndicatorService {

    public TrendlineService(StockDataRepository repository) {
        super(repository);
    }

    public BaseIndicatorResponse calculate(TrendlineEntry entry) {
        BarSeries series = loadSeries(entry.getSymbol().toUpperCase());
        return calculateWithSeries(entry, series);
    }

    public BaseIndicatorResponse calculateWithSeries(TrendlineEntry entry, BarSeries series) {
        String symbol = entry.getSymbol().toUpperCase();
        int period = entry.getPeriod();
        int lookback = entry.getLookback();
        LocalDateTime date = entry.getDate();
        int touchAmount = entry.getSupportResistanceTouchAmount();

        int targetIndex = seriesAmountValidator(symbol, series, date);

        // lookback period'da swing high ve low
        double swingHigh = findSwingHigh(series, targetIndex, lookback);
        double swingLow = findSwingLow(series, targetIndex, lookback);
        double currentPrice = series.getBar(targetIndex).getClosePrice().doubleValue();

        BaseIndicatorResponse result = new BaseIndicatorResponse();
        result.setSymbol(symbol);
        result.setIndicator("Trendline");
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());
        result.getValues().put("currentPrice", currentPrice);
        result.getValues().put("swingHigh", swingHigh);
        result.getValues().put("swingLow", swingLow);

        double highBreakout = (currentPrice - swingHigh) / swingHigh * 100;
        double lowBreakdown = (swingLow - currentPrice) / swingLow * 100;
        
        if (currentPrice > swingHigh) {
            if (highBreakout > 5) {
                result.setSignal("Strong Buy");
                result.setScore(3.0/3.0);
            } else if (highBreakout > 2) {
                result.setSignal("Buy");
                result.setScore(2.0/3.0);
            } else {
                result.setSignal("Weak Buy");
                result.setScore(1.0/3.0);
            }
        } else if (currentPrice < swingLow) {
            if (lowBreakdown > 5) {
                result.setSignal("Strong Sell");
                result.setScore(-3.0/3.0);
            } else if (lowBreakdown > 2) {
                result.setSignal("Sell");
                result.setScore(-2.0/3.0);
            } else {
                result.setSignal("Weak Sell");
                result.setScore(-1.0/3.0);
            }
        } else {
            result.setSignal("Hold");
            result.setScore(0.0);
        }

        String supportResistance = checkSupportResistance(series, targetIndex, lookback, swingHigh, swingLow, touchAmount);
        result.setStatus(supportResistance);
        
        int barsSinceSignal = calculateBarsSinceSignal(series, targetIndex, lookback, swingHigh, swingLow);
        result.setBarsSinceSignal(barsSinceSignal);
        
        return result;
    }

    private String checkSupportResistance(BarSeries series, int targetIndex, int lookback, double swingHigh, double swingLow, int touchAmount) {
        double tolerance = 0.02;
        int highTouches = 0;
        int lowTouches = 0;
        
        int start = Math.max(0, targetIndex - lookback);
        for (int i = start; i < targetIndex; i++) {
            double high = series.getBar(i).getHighPrice().doubleValue();
            double low = series.getBar(i).getLowPrice().doubleValue();
            
            if (Math.abs(high - swingHigh) / swingHigh <= tolerance) {
                highTouches++;
            }
            
            if (Math.abs(low - swingLow) / swingLow <= tolerance) {
                lowTouches++;
            }
        }

        if (highTouches >= touchAmount) {
            return "Resistance detected at " + swingHigh;
        }
        if (lowTouches >= touchAmount) {
            return "Support level detected at " + swingLow;
        }
        return "No significant support/resistance levels detected.";
    }

    private double findSwingHigh(BarSeries series, int targetIndex, int lookback) {
        double high = 0;
        int start = Math.max(0, targetIndex - lookback);
        for (int i = start; i < targetIndex; i++) {
            double barHigh = series.getBar(i).getHighPrice().doubleValue();
            if (barHigh > high) high = barHigh;
        }
        return high;
    }

    private double findSwingLow(BarSeries series, int targetIndex, int lookback) {
        double low = Double.MAX_VALUE;
        int start = Math.max(0, targetIndex - lookback);
        for (int i = start; i < targetIndex; i++) {
            double barLow = series.getBar(i).getLowPrice().doubleValue();
            if (barLow < low) low = barLow;
        }
        return low;
    }

    private int calculateBarsSinceSignal(BarSeries series, int targetIndex, int lookback, double swingHigh, double swingLow) {
        if (targetIndex <= 0) {
            return -1;
        }
        
        double currentPrice = series.getBar(targetIndex).getClosePrice().doubleValue();
        
        boolean currentAboveResistance = currentPrice > swingHigh;
        boolean currentBelowSupport = currentPrice < swingLow;
        
        if (!currentAboveResistance && !currentBelowSupport) {
            return -1;
        }
        
        int start = Math.max(0, targetIndex - lookback);
        
        for (int i = targetIndex - 1; i >= start; i--) {
            double prevClose = series.getBar(i).getClosePrice().doubleValue();
            
            if (currentAboveResistance) {
                // Resistance breakout
                if (prevClose <= swingHigh) {
                    return targetIndex - i;
                }
            } else if (currentBelowSupport) {
                // Support breakdown
                if (prevClose >= swingLow) {
                    return targetIndex - i;
                }
            }
        }
        
        return -1;
    }
}