package lion.mode.tradebot_backend.service.technicalanalysis.indicators;

import lion.mode.tradebot_backend.dto.indicators.MFIResult;
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

    public MFIResult calculateMFI(String symbol, int period, LocalDateTime date, int lowerLimit, int upperLimit) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < period + 1) {
            throw new NotEnoughDataException("Not enough data for MFI at " + date + " for " + symbol);
        }

        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            LocalDateTime barTime = series.getBar(i).getEndTime().toLocalDateTime();
            if (!barTime.isAfter(date)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1 || targetIndex < period) {
            throw new NotEnoughDataException("No bar found before or at " + date + " for " + symbol);
        }

        double mfiValue = computeMFI(series, period, targetIndex);

        return buildResult(symbol, period, mfiValue, lowerLimit, upperLimit);
    }

    private double computeMFI(BarSeries series, int period, int index) {
        double positiveFlow = 0.0;
        double negativeFlow = 0.0;

        for (int i = index - period + 1; i <= index; i++) {
            Bar currentBar = series.getBar(i);
            Bar prevBar = series.getBar(i - 1);

            double typicalPrice = (currentBar.getHighPrice().doubleValue()
                    + currentBar.getLowPrice().doubleValue()
                    + currentBar.getClosePrice().doubleValue()) / 3.0;

            double prevTypicalPrice = (prevBar.getHighPrice().doubleValue()
                    + prevBar.getLowPrice().doubleValue()
                    + prevBar.getClosePrice().doubleValue()) / 3.0;

            double rawMoneyFlow = typicalPrice * currentBar.getVolume().doubleValue();

            if (typicalPrice > prevTypicalPrice) {
                positiveFlow += rawMoneyFlow;
            } else if (typicalPrice < prevTypicalPrice) {
                negativeFlow += rawMoneyFlow;
            }
        }

        if (negativeFlow == 0) {
            return 100.0; // full positive flow MFI = 100
        }

        double moneyFlowRatio = positiveFlow / negativeFlow;
        return 100 - (100 / (1 + moneyFlowRatio));
    }

    private MFIResult buildResult(String symbol, int period, double mfiValue, int lowerLimit, int upperLimit) {
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
}
