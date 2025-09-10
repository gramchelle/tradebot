package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.indicator.ADXResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;

import java.time.LocalDateTime;

@Service
public class ADXService extends IndicatorService {

    public ADXService(StockDataRepository repository) {
        super(repository);
    }

    public ADXResult calculateADXAt(String symbol, int adxPeriod, int adxLookback, LocalDateTime targetDate) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < adxPeriod + adxLookback) {
            throw new NotEnoughDataException("Not enough data for ADX at " + targetDate + " for " + symbol);
        }

        int targetIndex = seriesAmountValidator(symbol, series, targetDate);

        ADXIndicator adx = new ADXIndicator(series, adxPeriod);
        PlusDIIndicator plusDI = new PlusDIIndicator(series, adxPeriod);

        double adxValue = adx.getValue(targetIndex).doubleValue();
        double prevAdx = targetIndex > 0 ? adx.getValue(targetIndex - 1).doubleValue() : adxValue;
        double plusDiValue = plusDI.getValue(targetIndex).doubleValue();

        ADXResult result = new ADXResult();
        result.setAdx(adxValue);
        result.setPrevAdx(prevAdx);
        result.setPlusDi(plusDiValue);
        result.setPeriod(adxPeriod);
        result.setLookback(adxLookback);

        if (adxValue > 38) result.setTrend("very strong");
        else if (adxValue > 25)  result.setTrend("strong");
        else result.setTrend("weak");

        return result;
    }

}
