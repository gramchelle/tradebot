package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.indicator.MFIResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.time.LocalDateTime;

@Service
public class MFIService extends IndicatorService{

    public MFIService(StockDataRepository repository) {
        super(repository);
    }

    public MFIResult calculateMfiWithSeries(String symbol, int period, LocalDateTime date, int lowerLimit, int upperLimit, BarSeries series) {
        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough data for MFI at " + date + " for " + symbol);

        int targetIndex = seriesAmountValidator(symbol, series, date);

        double positiveFlow = 0.0;
        double negativeFlow = 0.0;
        double mfiValue = 0;

        for (int i = targetIndex - period + 1; i <= targetIndex; i++) {
            Bar currentBar = series.getBar(i);
            Bar prevBar = series.getBar(i - 1);

            double typicalPrice = (currentBar.getHighPrice().doubleValue()
                    + currentBar.getLowPrice().doubleValue()
                    + currentBar.getClosePrice().doubleValue()) / 3.0;

            double prevTypicalPrice = (prevBar.getHighPrice().doubleValue()
                    + prevBar.getLowPrice().doubleValue()
                    + prevBar.getClosePrice().doubleValue()) / 3.0;

            double rawMoneyFlow = typicalPrice * currentBar.getVolume().doubleValue();

            if (typicalPrice > prevTypicalPrice) positiveFlow += rawMoneyFlow;
            else if (typicalPrice < prevTypicalPrice) negativeFlow += rawMoneyFlow;
        }

        if (negativeFlow == 0) {
            mfiValue = 100.0;
        }

        double moneyFlowRatio = positiveFlow / negativeFlow;
        mfiValue = 100 - (100 / (1 + moneyFlowRatio));

        MFIResult result = new MFIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setMfiValue(mfiValue);

        if (mfiValue > upperLimit) {
            result.setSignal("Sell");
            result.setScore(-1);
        } else if (mfiValue < lowerLimit) {
            result.setSignal("Buy");
            result.setScore(1);
        } else {
            result.setSignal("Hold");
            result.setScore(0);
        }
        return result;

    }

    public MFIResult calculateMFI(String symbol, int period, LocalDateTime date, int lowerLimit, int upperLimit) {
        BarSeries series = loadSeries(symbol);
        MFIResult result = calculateMfiWithSeries(symbol, period, date, lowerLimit, upperLimit, series);
        return result;        
    }

}
