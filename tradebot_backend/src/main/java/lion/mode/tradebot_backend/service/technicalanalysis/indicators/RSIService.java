package lion.mode.tradebot_backend.service.technicalanalysis.indicators;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.dto.indicators.RSIResult;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class RSIService extends IndicatorService {

    public RSIService(StockDataRepository repository) {
        super(repository);
    }

    public RSIResult calculateRSI(String symbol, int period, LocalDateTime targetDate, int trendPeriod, int lowerLimit, int upperLimit, int lookbackPeriod) {
        BarSeries series = loadSeries(symbol);
        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);

        if (series.getBarCount() < period + 1) {
            throw new NotEnoughDataException("Not enough data for RSI at " + targetDate + " for " + symbol);
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

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(close, period);

        double rsiValue = rsi.getValue(targetIndex).doubleValue();
        double onePeriodPrevRsi = rsi.getValue(targetIndex - trendPeriod).doubleValue();

        result.setRsiValue(rsiValue);
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());

        generateSignalScoreAndComment(result, rsiValue, onePeriodPrevRsi, lowerLimit, upperLimit);

        result.setDivergence(detectDivergence(close, rsi, targetIndex, lookbackPeriod));

        return result;
    }

    private void generateSignalScoreAndComment(RSIResult result, double rsiValue, double onePeriodPrevRsi, int lowerLimit, int upperLimit) {
        double rsi_diff = rsiValue - onePeriodPrevRsi;
        if (rsiValue > upperLimit) { //TODO: RSI's default values 70 and 30 will be parameterized.
            result.setSignal("Sell");
            result.setScore(-1);
            if ((rsi_diff) > 0){
                result.setTrendComment("RSI is above 70 and is increasing. You can also sell or hold.");
            } else {
                result.setTrendComment("RSI is above 70 and is decreasing. You can certainly sell.");
            }
        } else if (rsiValue < lowerLimit) {
            result.setSignal("Buy");
            result.setScore(1);
            if (rsi_diff > 0){
                result.setTrendComment("RSI is below 30 and is increasing. You can buy.");
            } else {
                result.setTrendComment("RSI is below 30 and is decreasing. You can buy or hold.");
            }
        } else {
            result.setSignal("Hold");
            result.setScore(0);
            if  (rsi_diff > 0){
                result.setTrendComment("RSI is between 30 and 70 and is increasing. You can hold.");
            } else {
                result.setTrendComment("RSI is between 30 and 70 and is decreasing. You can hold.");
            }

        }
    }

    //TBD this method later
    private void generateSignalAndScoreResult(RSIResult result, double rsiValue, double onePeriodPrevRsi) {
        double rsi_diff = rsiValue - onePeriodPrevRsi;
        if (rsiValue > 70) { //TODO: RSI's default values 70 and 30 will be parameterized.
            if ((rsi_diff) > 0){
                result.setSignal("Hold");
                result.setScore(0);
                result.setTrendComment("RSI is above 70 and is increasing. You can also sell or hold.");
            } else {
                result.setSignal("Sell");
                result.setScore(-1);
                result.setTrendComment("RSI is above 70 and is decreasing. You can sell.");
            }
        } else if (rsiValue < 30) {
            if (rsi_diff > 0){
                result.setSignal("Buy");
                result.setScore(1);
                result.setTrendComment("RSI is below 30 and is increasing. You can buy.");
            } else {
                result.setSignal("Hold");
                result.setScore(0);
                result.setTrendComment("RSI is below 30 and is decreasing. You can buy or hold.");
            }
        } else {
            result.setSignal("Hold");
            result.setScore(0);
            if (rsi_diff > 0){
                result.setTrendComment("RSI is between 30 and 70 and is increasing. You can hold.");
            } else {
                result.setTrendComment("RSI is between 30 and 70 and is decreasing. You can hold.");
            }

        }
    }

    private String detectDivergence(ClosePriceIndicator closePrice, RSIIndicator rsi, int endIndex, int lookbackPeriod) {
        int startIndex = Math.max(1, endIndex - lookbackPeriod);

        List<Integer> lowIndices = new ArrayList<>();
        List<Integer> highIndices = new ArrayList<>();

        // Local min/max
        for (int i = startIndex + 1; i < endIndex; i++) {
            double prevPrice = closePrice.getValue(i - 1).doubleValue();
            double price = closePrice.getValue(i).doubleValue();
            double nextPrice = closePrice.getValue(i + 1).doubleValue();

            if (price < prevPrice && price < nextPrice) {
                lowIndices.add(i);
            }
            if (price > prevPrice && price > nextPrice) {
                highIndices.add(i);
            }
        }

        // Bullish divergence
        for (int i = 0; i < lowIndices.size() - 1; i++) {
            for (int j = i + 1; j < lowIndices.size(); j++) {
                int idx1 = lowIndices.get(i);
                int idx2 = lowIndices.get(j);
                double priceDiff = closePrice.getValue(idx1).doubleValue() - closePrice.getValue(idx2).doubleValue();
                double rsiDiff = rsi.getValue(idx2).doubleValue() - rsi.getValue(idx1).doubleValue();

                if (priceDiff > 0 && rsiDiff > 0) {
                    return "Bullish";
                }
            }
        }

        // Bearish divergence
        for (int i = 0; i < highIndices.size() - 1; i++) {
            for (int j = i + 1; j < highIndices.size(); j++) {
                int idx1 = highIndices.get(i);
                int idx2 = highIndices.get(j);
                double priceDiff = closePrice.getValue(idx2).doubleValue() - closePrice.getValue(idx1).doubleValue();
                double rsiDiff = rsi.getValue(idx1).doubleValue() - rsi.getValue(idx2).doubleValue();

                if (priceDiff > 0 && rsiDiff > 0) {
                    return "Bearish";
                }
            }
        }

        return "None";
    }

}
