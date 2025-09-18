package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.DMIEntry;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.adx.MinusDIIndicator;

import java.time.LocalDateTime;

@Service
public class DMIService extends IndicatorService {

    public DMIService(StockDataRepository repository) {
        super(repository);
    }

    public BaseIndicatorResponse calculate(DMIEntry entry){
        BarSeries series = loadSeries(entry.getSymbol().toUpperCase());
        return calculateWithSeries(entry, series);
    }

    public BaseIndicatorResponse calculateWithSeries(DMIEntry entry, BarSeries series) {
        String symbol = entry.getSymbol().toUpperCase();
        int period = entry.getPeriod();
        LocalDateTime date = entry.getDate();
        double strongTrendThreshold = entry.getStrongTrendThreshold();
        double moderateTrendThreshold = entry.getModerateTrendThreshold();
        double diDiff = entry.getSignificantDiDiff();

        if (series.getBarCount() < period + 2) {
            throw new NotEnoughDataException("Not enough data for DMI at " + date + " for " + symbol);
        }

        int targetIndex = seriesAmountValidator(symbol, series, date);
        if (targetIndex < 2) {
            throw new NotEnoughDataException("Target index " + targetIndex + " is insufficient for DMI trend analysis for " + symbol);
        }

        ADXIndicator adx = new ADXIndicator(series, period);
        PlusDIIndicator plusDI = new PlusDIIndicator(series, period);
        MinusDIIndicator minusDI = new MinusDIIndicator(series, period);

        Num plusDiValue = plusDI.getValue(targetIndex);
        Num minusDiValue = minusDI.getValue(targetIndex);
        Num adxValue = adx.getValue(targetIndex);
        Num prevAdx = adx.getValue(targetIndex - 1);
        Num prev2Adx = adx.getValue(targetIndex - 2);

        BaseIndicatorResponse result = new BaseIndicatorResponse();
        result.setSymbol(symbol);
        result.setIndicator("DMI");
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());
        result.getValues().put("adxValue", adxValue.doubleValue());
        result.getValues().put("plusDI", plusDiValue.doubleValue());
        result.getValues().put("minusDI", minusDiValue.doubleValue());
        result.setStatus(detectAdxTrend(adxValue.doubleValue(), prevAdx.doubleValue(), prev2Adx.doubleValue()));
        result.getValues().put("diDifference", plusDiValue.doubleValue() - minusDiValue.doubleValue());

        generateDmiSignal(result, strongTrendThreshold, moderateTrendThreshold, diDiff, plusDiValue.doubleValue(), minusDiValue.doubleValue(), adxValue.doubleValue());
        int barsSinceSignal = calculateBarsSinceSignal(plusDI, minusDI, targetIndex);
        result.setBarsSinceSignal(barsSinceSignal);
        return result;
    }

    private void generateDmiSignal(BaseIndicatorResponse result, double strongThresh, double moderateThresh, double diDiff, double plusDI, double minusDI, double adx) {
        double diDifference = Math.abs(plusDI - minusDI);

        if (adx >= strongThresh) {
            if (plusDI > minusDI) {
                result.setSignal(diDifference >= diDiff ? "Strong Buy" : "Buy");
                result.setScore(diDifference >= diDiff ? 1.0 : 2.0/3.0);
            } else if (minusDI > plusDI) {
                result.setSignal(diDifference >= diDiff ? "Strong Sell" : "Sell");
                result.setScore(diDifference >= diDiff ? -1.0 : -2.0/3.0);
            } else {
                result.setSignal("Hold");
                result.setScore(0.0);
            }
        } else if (adx >= moderateThresh) {
            if (plusDI > minusDI) {
                result.setSignal("Weak Buy");
                result.setScore(1.0/3.0);
            } else if (minusDI > plusDI) {
                result.setSignal("Weak Sell");
                result.setScore(-1.0/3.0);
            } else {
                result.setSignal("Hold");
                result.setScore(0.0);
            }
        } else {
            result.setSignal("Hold - No Clear Trend");
            result.setScore(0.0);
        }
    }

    private int calculateBarsSinceSignal(PlusDIIndicator plusDI, MinusDIIndicator minusDI, int targetIndex) {
        if (targetIndex <= 0) {
            return -1;
        }
        
        double currentPlusDI = plusDI.getValue(targetIndex).doubleValue();
        double currentMinusDI = minusDI.getValue(targetIndex).doubleValue();
        
        if (Math.abs(currentPlusDI - currentMinusDI) < 0.5) {
            return -1;
        }
        
        boolean currentlyBullish = currentPlusDI > currentMinusDI;
        
        for (int i = targetIndex - 1; i >= 0; i--) {
            double prevPlusDI = plusDI.getValue(i).doubleValue();
            double prevMinusDI = minusDI.getValue(i).doubleValue();
            boolean wasBullish = prevPlusDI > prevMinusDI;
            
            if (currentlyBullish != wasBullish) {
                return targetIndex - i;
            }
        }
        
        return -1;
    }

    private String detectAdxTrend(double currentAdx, double prevAdx, double prev2Adx) {
        double recentChange = currentAdx - prevAdx;
        double previousChange = prevAdx - prev2Adx;

        final double MIN_CHANGE_THRESHOLD = 1.0;

        if (recentChange > MIN_CHANGE_THRESHOLD && previousChange > MIN_CHANGE_THRESHOLD) {
            return "ADX Strengthening (Trend Building)";
        } else if (recentChange < -MIN_CHANGE_THRESHOLD && previousChange < -MIN_CHANGE_THRESHOLD) {
            return "ADX Weakening (Trend Fading)";
        } else if (Math.abs(recentChange) <= MIN_CHANGE_THRESHOLD && Math.abs(previousChange) <= MIN_CHANGE_THRESHOLD) {
            return "ADX Stable";
        } else {
            return "ADX Transitioning";
        }
    }


}
