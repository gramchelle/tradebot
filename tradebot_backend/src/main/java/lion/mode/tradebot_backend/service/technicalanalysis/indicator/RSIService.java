package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.RSIEntry;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
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

    public BaseIndicatorResponse calculate(RSIEntry rsiEntry) {
        try {
            if (rsiEntry.getSymbol() == null || rsiEntry.getSymbol().trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }

            BarSeries series = loadSeries(rsiEntry.getSymbol().toUpperCase());
            return calculateWithSeries(rsiEntry, series);

        } catch (NotEnoughDataException e) {
            throw new NotEnoughDataException("Not enough data/Invalid symbol to calculate RSI for symbol: " + rsiEntry.getSymbol());

        } catch (Exception e) {
            System.err.println("Error calculating RSI: " + e.getMessage());
            return null;
        }
    }

    public BaseIndicatorResponse calculateWithSeries(RSIEntry rsiEntry, BarSeries series) {
        int previousBarsSinceSignal = -1;

        if (rsiEntry.getLowerLimit() >= rsiEntry.getUpperLimit())
            throw new IllegalArgumentException("Lower limit must be less than upper limit");

        // RSIEntry Dto Fields
        String symbol = rsiEntry.getSymbol().toUpperCase();
        LocalDateTime date = rsiEntry.getDate();
        String source = rsiEntry.getSource();
        int period = rsiEntry.getPeriod();
        int lowerLimit = rsiEntry.getLowerLimit();
        int upperLimit = rsiEntry.getUpperLimit();

        // Build BaseIndicatorResponse Dto
        BaseIndicatorResponse result = new BaseIndicatorResponse();
        result.setSymbol(rsiEntry.getSymbol());
        result.setIndicator("RSI");

        // Check for bars
        if (series.getBarCount() < period + 1) {
            result.getErrors().put("INSUFFICIENT_DATA", "Not enough data to calculate RSI");
            return result;
        }

        int targetIndex = seriesAmountValidator(symbol, series, date);

        // Select source type and calculate RSI
        Indicator<Num> prices = sourceSelector(source, series);
        RSIIndicator rsi = new RSIIndicator(prices, period);

        double rsiValue = rsi.getValue(targetIndex).doubleValue();
        int barsSinceSignal = calculateBarsSinceSignal(rsi, lowerLimit, upperLimit, targetIndex);
        generateAdvancedSignalAndScore(result, rsiValue, lowerLimit, upperLimit, barsSinceSignal);

        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());
        result.getValues().put("rsiValue", rsiValue);

        return result;
    }

    private void generateAdvancedSignalAndScore(BaseIndicatorResponse result, double rsiValue, int lowerLimit, int upperLimit, int barsSinceSignal) {
        double weakThreshold = (upperLimit - lowerLimit) / 8.0;
        double strongThreshold = 10.0;
        String signal;
        double score;

        if (rsiValue > upperLimit) {
            if (rsiValue > upperLimit + strongThreshold) {
                signal = "Strong Sell";
                score = -3.0;
            } else {
                signal = "Sell";
                score = -2.0;
            }
        } else if (rsiValue < lowerLimit) {
            if (rsiValue < lowerLimit - strongThreshold) {
                signal = "Strong Buy";
                score = 3.0;
            } else {
                score = 2.0;
                signal = "Buy";
            }
        } else {
            if (rsiValue < (lowerLimit + weakThreshold)){
                score = 1.0;
                signal = "Weak Buy";
            } else if (rsiValue > (upperLimit - weakThreshold)){
                score = -1.0;
                signal = "Weak Sell";
            } else {
                score = 0.0;
                signal = "Hold";
            }
        }

        result.setSignal(signal);
        result.setScore(score/3.0);
        result.setBarsSinceSignal(barsSinceSignal);

    }

    private int calculateBarsSinceSignal(RSIIndicator rsi, int lowerLimit, int upperLimit, int targetIndex) {
        if (targetIndex <= 0) return -1;
        
        double currentRSI = rsi.getValue(targetIndex).doubleValue();

        if (currentRSI <= upperLimit && currentRSI >= lowerLimit) return -1;
        
        for (int i = targetIndex - 1; i >= 0; i--) {
            double previousRSI = rsi.getValue(i).doubleValue();
            
            if (currentRSI > upperLimit) {
                if (previousRSI <= upperLimit) {
                    return targetIndex - i;
                }
            } else if (currentRSI < lowerLimit) {
                if (previousRSI >= lowerLimit) {
                    return targetIndex - i;
                }
            }
        }
        return -1;
    }

    // TODO divergence için ayrı bir endpoint oluşturulabilir
    private String detectDivergence(Indicator<Num> prices, RSIIndicator rsi, int endIndex, int lookbackPeriod) {
        int startIndex = Math.max(2, endIndex - lookbackPeriod);
        int minDistance = 5; // Minimum periods between peaks

        List<Integer> lows = new ArrayList<>();
        List<Integer> highs = new ArrayList<>();

        for (int i = startIndex + 2; i < endIndex - 2; i++) {
            double current = prices.getValue(i).doubleValue();

            boolean isLow = current < prices.getValue(i - 2).doubleValue() &&
                    current < prices.getValue(i - 1).doubleValue() &&
                    current < prices.getValue(i + 1).doubleValue() &&
                    current < prices.getValue(i + 2).doubleValue();

            boolean isHigh = current > prices.getValue(i - 2).doubleValue() &&
                    current > prices.getValue(i - 1).doubleValue() &&
                    current > prices.getValue(i + 1).doubleValue() &&
                    current > prices.getValue(i + 2).doubleValue();

            if (isLow && (lows.isEmpty() || (i - lows.get(lows.size() - 1)) >= minDistance)) {
                lows.add(i);
            }
            if (isHigh && (highs.isEmpty() || (i - highs.get(highs.size() - 1)) >= minDistance)) {
                highs.add(i);
            }
        }

        // bullish divergence
        if (lows.size() >= 2) {
            for (int i = lows.size() - 2; i >= Math.max(0, lows.size() - 4); i--) {
                int earlierLow = lows.get(i);
                int laterLow = lows.get(lows.size() - 1);

                double earlierPrice = prices.getValue(earlierLow).doubleValue();
                double laterPrice = prices.getValue(laterLow).doubleValue();
                double earlierRSI = rsi.getValue(earlierLow).doubleValue();
                double laterRSI = rsi.getValue(laterLow).doubleValue();

                if (laterPrice < earlierPrice && laterRSI > earlierRSI &&
                        (earlierRSI < 35 || laterRSI < 35)) {
                    return "Bullish";
                }
            }
        }

        if (highs.size() >= 2) {
            for (int i = highs.size() - 2; i >= Math.max(0, highs.size() - 4); i--) {
                int earlierHigh = highs.get(i);
                int laterHigh = highs.get(highs.size() - 1);

                double earlierPrice = prices.getValue(earlierHigh).doubleValue();
                double laterPrice = prices.getValue(laterHigh).doubleValue();
                double earlierRSI = rsi.getValue(earlierHigh).doubleValue();
                double laterRSI = rsi.getValue(laterHigh).doubleValue();

                if (laterPrice > earlierPrice && laterRSI < earlierRSI &&
                        (earlierRSI > 65 || laterRSI > 65)) {
                    return "Bearish";
                }
            }
        }

        return "None";
    }
}
