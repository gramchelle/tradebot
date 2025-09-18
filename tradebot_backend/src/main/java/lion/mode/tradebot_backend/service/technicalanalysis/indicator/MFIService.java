package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.MFIEntry;
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

    public BaseIndicatorResponse calculate(MFIEntry entry) {
        BarSeries series = loadSeries(entry.getSymbol().toUpperCase());
        return calculateWithSeries(entry, series);
    }

    public BaseIndicatorResponse calculateWithSeries(MFIEntry entry, BarSeries series) {
        String symbol = entry.getSymbol().toUpperCase();
        LocalDateTime date = entry.getDate();
        int period = entry.getPeriod();
        int upperLimit = entry.getUpperLimit();
        int lowerLimit = entry.getLowerLimit();

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough data for MFI at " + date + " for " + symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        if (targetIndex < period) throw new NotEnoughDataException("Target index " + targetIndex + " is less than required period " + period + " for " + symbol);

        double positiveFlow = 0.0;
        double negativeFlow = 0.0;

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

            if (typicalPrice > prevTypicalPrice) {
                positiveFlow += rawMoneyFlow;
            } else if (typicalPrice < prevTypicalPrice) {
                negativeFlow += rawMoneyFlow;
            }
        }

        double mfiValue;
        
        if (negativeFlow == 0.0) {
            if (positiveFlow == 0.0) {
                mfiValue = 50.0;
            } else {
                mfiValue = 100.0;
            }
        } else {
            double moneyFlowRatio = positiveFlow / negativeFlow;
            mfiValue = 100.0 - (100.0 / (1.0 + moneyFlowRatio));
        }

        BaseIndicatorResponse result = new BaseIndicatorResponse();
        result.setSymbol(symbol);
        result.setIndicator("MFI");
        result.getValues().put("mfiValue", mfiValue);
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());

        generateAdvancedSignalAndScore(result, mfiValue, lowerLimit, upperLimit);
        int barsSinceSignal = calculateBarsSinceSignal(series, targetIndex, period, lowerLimit, upperLimit);
        result.setBarsSinceSignal(barsSinceSignal);
        return result;
    }

    private void generateAdvancedSignalAndScore(BaseIndicatorResponse result, double value, int lowerLimit, int upperLimit) {
        double weakThreshold = (upperLimit - lowerLimit) / 15.0; // Threshold for weak signals
        double strongThreshold = 10.0; // Threshold for strong signals
        
        if (value > upperLimit) {
            if (value > upperLimit + strongThreshold) {
                result.setScore(-3.0/3.0);
                result.setSignal("Strong Sell");
            } else {
                result.setScore(-2.0/3.0);
                result.setSignal("Sell");
            }
        } else if (value < lowerLimit) {
            if (value < lowerLimit - strongThreshold) {
                result.setScore(1.0);
                result.setSignal("Strong Buy");
            } else {
                result.setScore(2.0/3.0);
                result.setSignal("Buy");
            }
        } else {
            if (value < (lowerLimit + weakThreshold)){
                result.setScore(1.0/3.0);
                result.setSignal("Weak Buy");
            } else if (value > (upperLimit - weakThreshold)){
                result.setScore(-1.0/3.0);
                result.setSignal("Weak Sell");
            } else {
                result.setScore(0.0);
                result.setSignal("Hold");
            }
        }
    }

    private int calculateBarsSinceSignal(BarSeries series, int targetIndex, int period, int lowerLimit, int upperLimit) {
        if (targetIndex <= period) return -1;

        double currentMFI = calculateMFIValue(series, targetIndex, period);
        
        boolean currentInSignal = (currentMFI > upperLimit) || (currentMFI < lowerLimit);
        
        if (!currentInSignal) return -1;
        
        for (int i = targetIndex - 1; i >= period; i--) {
            double prevMFI = calculateMFIValue(series, i, period);
            
            if (currentMFI > upperLimit) {
                if (prevMFI <= upperLimit) {
                    return targetIndex - i;
                }
            } else if (currentMFI < lowerLimit) {
                if (prevMFI >= lowerLimit) {
                    return targetIndex - i;
                }
            }
        }
        
        return -1;
    }

    private double calculateMFIValue(BarSeries series, int targetIndex, int period) {
        double positiveFlow = 0.0;
        double negativeFlow = 0.0;

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

            if (typicalPrice > prevTypicalPrice) {
                positiveFlow += rawMoneyFlow;
            } else if (typicalPrice < prevTypicalPrice) {
                negativeFlow += rawMoneyFlow;
            }
        }

        if (negativeFlow == 0.0) {
            if (positiveFlow == 0.0) {
                return 50.0; // nötr MFI
            } else {
                return 100.0; // positive flow
            }
        } else {
            double moneyFlowRatio = positiveFlow / negativeFlow;
            return 100.0 - (100.0 / (1.0 + moneyFlowRatio));
        }
    }

}
