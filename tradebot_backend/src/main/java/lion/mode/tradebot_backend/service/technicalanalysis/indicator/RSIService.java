package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.dto.indicator.RSIResult;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.RSIIndicator;
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
        RSIResult result = calculateRSIWithSeries(symbol, period, date, lowerLimit, upperLimit, priceType, series);
        return result;
    }

    public RSIResult calculateRSIWithSeries(String symbol, int period, LocalDateTime date, int lowerLimit, int upperLimit, String priceType, BarSeries series) {
        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);

        if (series.getBarCount() < period + 1) throw new NotEnoughDataException("Not enough data for RSI at " + date + " for " + symbol);

        int targetIndex = seriesAmountValidator(symbol, series, date);

        Indicator<Num> prices = priceTypeSelector(priceType, series);
        RSIIndicator rsi = new RSIIndicator(prices, period);

        double rsiValue = rsi.getValue(targetIndex).doubleValue();

        result.setRsiValue(rsiValue);
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());

        generateSignalScoreAndComment(result, rsiValue, lowerLimit, upperLimit);

        return result;
    }

    private void generateSignalScoreAndComment(RSIResult result, double rsiValue, int lowerLimit, int upperLimit) {
        if (rsiValue > upperLimit) {
            result.setSignal("Sell");
            result.setScore(-1);
        } else if (rsiValue < lowerLimit) {
            result.setSignal("Buy");
            result.setScore(1);
        } else {
            result.setSignal("Hold");
            result.setScore(0);
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
