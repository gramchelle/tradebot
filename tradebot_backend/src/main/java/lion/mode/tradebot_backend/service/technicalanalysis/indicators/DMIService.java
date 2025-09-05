package lion.mode.tradebot_backend.service.technicalanalysis.indicators;

import lion.mode.tradebot_backend.dto.indicators.DMIResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;

import java.time.LocalDateTime;

@Service
public class DMIService extends IndicatorService {

    public DMIService(StockDataRepository repository) {
        super(repository);
    }

    public DMIResult calculateDMI(String symbol, int period, LocalDateTime date) {
        BarSeries series = loadSeries(symbol);
        if (series.getBarCount() < period + 1) {
            throw new NotEnoughDataException("Not enough data for DMI at " + date + " for " + symbol);
        }

        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            LocalDateTime barTime = series.getBar(i).getEndTime().toLocalDateTime();
            if (!barTime.isAfter(date)) {
                targetIndex = i;
            } else break;
        }
        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found before or at " + date + " for " + symbol);
        }

        ADXIndicator adx = new ADXIndicator(series, period);
        PlusDIIndicator plusDI = new PlusDIIndicator(series, period);
        MinusDIIndicator minusDI = new MinusDIIndicator(series, period);

        double plusDiValue = plusDI.getValue(targetIndex).doubleValue();
        double minusDiValue = minusDI.getValue(targetIndex).doubleValue();
        double adxValue = adx.getValue(targetIndex).doubleValue();
        double prevAdx = adx.getValue(targetIndex - 1).doubleValue();
        double prev2Adx = adx.getValue(targetIndex - 2).doubleValue();

        DMIResult result = new DMIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setPlusDi(plusDiValue);
        result.setMinusDi(minusDiValue);
        result.setAdxScore(adxValue);
        result.setAdxTrend(detectAdxTrend(adxValue, prevAdx, prev2Adx));

        if (adxValue > 25) {
            if (plusDiValue > minusDiValue) {
                result.setSignal("Sell");
                result.setScore(-1);
            } else if (plusDiValue < minusDiValue) {
                result.setSignal("Buy");
                result.setScore(1);
            } else {
                result.setSignal("Hold");
                result.setScore(0);
            }
        } else {
            if (plusDiValue > minusDiValue) {
                result.setSignal("Sell -> Weak Trend");
                result.setScore(-1);
            } else if (plusDiValue < minusDiValue) {
                result.setSignal("Buy -> Weak Trend");
                result.setScore(1);
            } else {
                result.setSignal("Hold");
                result.setScore(0);
            }
        }

        return result;
    }

    private String detectAdxTrend(double currentAdx, double prevAdx, double prev2Adx){
        double diff =  currentAdx - prevAdx;
        double diff2 = prevAdx - prev2Adx;

        if (diff > 0 && diff2 > 0){
            return "Uptrend";
        }  else if (diff < 0 && diff2 < 0){
            return "Downtrend";
        } else {
            return "Trend is stable";
        }
    }

}
