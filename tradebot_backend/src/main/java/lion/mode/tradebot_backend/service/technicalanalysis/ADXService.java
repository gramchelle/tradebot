package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.indicators.ADXResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;

import java.time.LocalDateTime;

@Service
public class ADXService extends IndicatorService {

    public ADXService(StockDataRepository repository) {
        super(repository);
    }

    public ADXResult calculateADX(String symbol, int adxPeriod, int adxLookback) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < adxPeriod + adxLookback) {
            throw new NotEnoughDataException("Not enough data for ADX calculation for " + symbol);
        }

        ADXIndicator adx = new ADXIndicator(series, adxPeriod);
        PlusDIIndicator plusDI = new PlusDIIndicator(series, adxPeriod);
        MinusDIIndicator minusDI = new MinusDIIndicator(series, adxPeriod);

        int lastIndex = series.getEndIndex();
        double adxValue = adx.getValue(lastIndex).doubleValue();
        double prevAdx = adx.getValue(lastIndex - 1).doubleValue();
        double prev2Adx = adx.getValue(lastIndex - 2).doubleValue();
        double plusDiValue = plusDI.getValue(lastIndex).doubleValue();

        ADXResult result = new ADXResult();
        result.setAdx(adxValue);
        result.setPrevAdx(prevAdx);
        result.setPrev2Adx(prev2Adx);
        result.setPlusDi(plusDiValue);
        result.setAdxPeriod(adxPeriod);
        result.setAdxLookback(adxLookback);

        if (adxValue > prev2Adx && adxValue > 25) {
            result.setSignal("strong uptrend");
            result.setScore(1);
        } else if (adxValue < prev2Adx && adxValue > 25) {
            result.setSignal("strong downtrend");
            result.setScore(-1);
        } else if (adxValue < prev2Adx && adxValue < 25) {
            result.setSignal("weak downtrend");
            result.setScore(0);
        }  else {
            result.setSignal("weak uptrend");
        }

        return result;
    }

    public ADXResult calculateADXAt(String symbol, int adxPeriod, int adxLookback, LocalDateTime targetDate) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < adxPeriod + adxLookback) {
            throw new NotEnoughDataException("Not enough data for ADX at " + targetDate + " for " + symbol);
        }

        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            LocalDateTime barTime = series.getBar(i).getEndTime().toLocalDateTime();
            if (!barTime.isAfter(targetDate)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found before or at " + targetDate + " for " + symbol);
        }

        ADXIndicator adx = new ADXIndicator(series, adxPeriod);
        PlusDIIndicator plusDI = new PlusDIIndicator(series, adxPeriod);

        double adxValue = adx.getValue(targetIndex).doubleValue();
        double prevAdx = targetIndex > 0 ? adx.getValue(targetIndex - 1).doubleValue() : adxValue;
        double prev2Adx = targetIndex > 1 ? adx.getValue(targetIndex - 2).doubleValue() : prevAdx;
        double plusDiValue = plusDI.getValue(targetIndex).doubleValue();

        ADXResult result = new ADXResult();
        result.setAdx(adxValue);
        result.setPrevAdx(prevAdx);
        result.setPrev2Adx(prev2Adx);
        result.setPlusDi(plusDiValue);
        result.setAdxPeriod(adxPeriod);
        result.setAdxLookback(adxLookback);

        if (adxValue > prev2Adx && adxValue > 25) {
            result.setSignal("strong uptrend");
            result.setScore(1);
        } else if (adxValue < prev2Adx && adxValue > 25) {
            result.setSignal("strong downtrend");
            result.setScore(-1);
        } else if (adxValue < prev2Adx && adxValue < 25) {
            result.setSignal("weak downtrend");
            result.setScore(0);
        }  else {
            result.setSignal("weak uptrend");
        }

        return result;
    }
}
