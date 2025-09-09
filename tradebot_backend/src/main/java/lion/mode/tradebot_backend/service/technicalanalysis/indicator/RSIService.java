package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.dto.indicator.RSIResult;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class RSIService extends IndicatorService {

    public RSIService(StockDataRepository repository) {
        super(repository);
    }

    public RSIResult calculateRSI(String symbol, int period, LocalDateTime date, int trendPeriod, int lowerLimit, int upperLimit, int lookbackPeriod, String priceType) {
        BarSeries series = loadSeries(symbol);
        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough data for RSI at " + date + " for " + symbol);

        int targetIndex = seriesAmountValidator(symbol, series, date);

        Indicator<Num> prices = priceTypeSelector(priceType, series);
        RSIIndicator rsi = new RSIIndicator(prices, period);

        double rsiValue = rsi.getValue(targetIndex).doubleValue();
        double onePeriodPrevRsi = rsi.getValue(targetIndex - trendPeriod).doubleValue();

        result.setRsiValue(rsiValue);
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());

        generateSignalScoreAndComment(result, rsiValue, onePeriodPrevRsi, lowerLimit, upperLimit);

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

    // TODO: Refine logic
    private String detectDivergence(Indicator<Num> prices, RSIIndicator rsi, int endIndex, int lookbackPeriod) {
        int startIndex = Math.max(1, endIndex - lookbackPeriod);

        List<Integer> lowIndices = new ArrayList<>();
        List<Integer> highIndices = new ArrayList<>();

        // Local min/max
        for (int i = startIndex + 1; i < endIndex; i++) {
            double prevPrice = prices.getValue(i - 1).doubleValue();
            double price = prices.getValue(i).doubleValue();
            double nextPrice = prices.getValue(i + 1).doubleValue();

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
                double priceDiff = prices.getValue(idx1).doubleValue() - prices.getValue(idx2).doubleValue();
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
                double priceDiff = prices.getValue(idx2).doubleValue() - prices.getValue(idx1).doubleValue();
                double rsiDiff = rsi.getValue(idx1).doubleValue() - rsi.getValue(idx2).doubleValue();

                if (priceDiff > 0 && rsiDiff > 0) {
                    return "Bearish";
                }
            }
        }

        return "None";
    }

}
