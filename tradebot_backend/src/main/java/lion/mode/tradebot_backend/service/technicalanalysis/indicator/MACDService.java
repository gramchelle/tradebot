package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.MACDEntry;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.service.technicalanalysis.IndicatorService;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MACDService extends IndicatorService {

    public MACDService(StockDataRepository repository) {
        super(repository);
    }

    public BaseIndicatorResponse calculate(MACDEntry entry) {
        try {
            if (entry.getSymbol() == null || entry.getSymbol().trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }

            BarSeries series = loadSeries(entry.getSymbol());
            return calculateWithSeries(entry, series);

        } catch (NotEnoughDataException e) {
            throw new NotEnoughDataException("Not enough data to calculate MACD for symbol: " + entry.getSymbol() + ". " + e.getMessage());

        } catch (Exception e) {
            throw new RuntimeException("Error calculating MACD for symbol: " + entry.getSymbol() + ". " + e.getMessage(), e);
        }
    }

    public BaseIndicatorResponse calculateWithSeries(MACDEntry macdEntry, BarSeries series) {
        String symbol = macdEntry.getSymbol();
        int shortPeriod = macdEntry.getShortPeriod();
        int longPeriod = macdEntry.getLongPeriod();
        int signalPeriod = macdEntry.getSignalPeriod();
        LocalDateTime date = macdEntry.getDate();
        int histogramTrendPeriod = macdEntry.getHistogramTrendPeriod();
        double histogramConfidence = macdEntry.getHistogramConfidence();
        String priceType = macdEntry.getSource();
        
        if (series == null || series.isEmpty()) throw new IllegalArgumentException("No data available for symbol: " + symbol);
        
        int minRequiredBars = longPeriod + signalPeriod + 2; // +
        if (series.getBarCount() < minRequiredBars) throw new NotEnoughDataException("Not enough data for MACD. Required: " + minRequiredBars + ", Available: " + series.getBarCount() + " for " + symbol);
        
        int targetIndex = seriesAmountValidator(symbol, series, date);
        
        if (targetIndex < longPeriod + signalPeriod - 1) throw new NotEnoughDataException("Target index " + targetIndex + " insufficient for MACD calculation for " + symbol);

        Indicator<Num> prices = sourceSelector(priceType, series);
        MACDIndicator macd = new MACDIndicator(prices, shortPeriod, longPeriod);
        EMAIndicator signal = new EMAIndicator(macd, signalPeriod);

        double macdValue = macd.getValue(targetIndex).doubleValue();
        double signalValue = signal.getValue(targetIndex).doubleValue();
        double histogramValue = macdValue - signalValue;
        
        double prevMacdValue = macd.getValue(targetIndex - 1).doubleValue();
        double prevSignalValue = signal.getValue(targetIndex - 1).doubleValue();
        double prevHistogramValue = prevMacdValue - prevSignalValue;

        BaseIndicatorResponse result = new BaseIndicatorResponse();
        result.setSymbol(symbol);
        result.setIndicator("MACD");
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());
        result.getValues().put("macdValue", macdValue);
        result.getValues().put("signalValue", signalValue);
        result.getValues().put("histogramValue", histogramValue);

        generateMacdSignal(result, macdValue, signalValue, histogramValue, prevMacdValue, prevSignalValue, prevHistogramValue);

        double histogramTrend = detectHistogramSlope(series, macd, signal, histogramTrendPeriod, histogramConfidence, date);
        result.getValues().put("histogramTrend", histogramTrend);
        int barsSinceSignal = calculateBarsSinceSignal(macd, signal, targetIndex);
        result.setBarsSinceSignal(barsSinceSignal);

        return result;
    }

    private void generateMacdSignal(BaseIndicatorResponse result, double macd, double signal, double histogram, double prevMacd, double prevSignal, double prevHistogram){
        boolean bullishCrossover = (prevMacd <= prevSignal) && (macd > signal);
        boolean bearishCrossover = (prevMacd >= prevSignal) && (macd < signal);
        
        boolean macdAboveZero = macd > 0;
        boolean macdBelowZero = macd < 0;
        boolean prevMacdAboveZero = prevMacd > 0;
        boolean prevMacdBelowZero = prevMacd < 0;
        
        boolean bullishZeroCross = prevMacdBelowZero && macdAboveZero;
        boolean bearishZeroCross = prevMacdAboveZero && macdBelowZero;
        
        // Histogram momentum
        boolean histogramIncreasing = histogram > prevHistogram;
        boolean histogramDecreasing = histogram < prevHistogram;
        
        if (bullishCrossover) {
            if (macdAboveZero) {
                result.setSignal("Strong Buy");
                result.setScore(1.0);
            } else {
                result.setSignal("Buy");
                result.setScore(2.0/3.0);
            }
        } else if (bearishCrossover) {
            if (macdBelowZero) {
                result.setSignal("Strong Sell");
                result.setScore(-1.0);
            } else {
                result.setSignal("Sell");
                result.setScore(-2.0/3.0);
            }
        } else if (bullishZeroCross) {
            result.setSignal("Buy - Zero Line Cross");
            result.setScore(2.0/3.0);
        } else if (bearishZeroCross) {
            result.setSignal("Sell - Zero Line Cross");
            result.setScore(-2.0/3.0);
        } else {
            if (macd > signal) {
                if (histogramIncreasing) {
                    result.setSignal("Weak Buy");
                    result.setScore(1.0/3.0);
                } else {
                    result.setSignal("Hold - Bullish");
                    result.setScore(0.0);
                }
            } else {
                if (histogramDecreasing) {
                    result.setSignal("Weak Sell");
                    result.setScore(-1.0/3.0);
                } else {
                    result.setSignal("Hold - Bearish");
                    result.setScore(0.0);
                }
            }
        }
    }

    private int calculateBarsSinceSignal(MACDIndicator macd, EMAIndicator signal, int targetIndex) {
        if (targetIndex <= 0) {
            return -1;
        }
        
        double currentMacd = macd.getValue(targetIndex).doubleValue();
        double currentSignal = signal.getValue(targetIndex).doubleValue();
        
        if (Math.abs(currentMacd - currentSignal) < 0.001) {
            return -1;
        }
        
        boolean currentlyBullish = currentMacd > currentSignal;
        
        for (int i = targetIndex - 1; i >= 0; i--) {
            double prevMacd = macd.getValue(i).doubleValue();
            double prevSignal = signal.getValue(i).doubleValue();
            boolean wasBullish = prevMacd > prevSignal;
            
            if (currentlyBullish != wasBullish) {
                return targetIndex - i;
            }
        }
        
        return -1;
    }

    private double detectHistogramSlope(BarSeries series, MACDIndicator macd, EMAIndicator signal, int trendPeriod, double confidence, LocalDateTime date) {
        int targetIndex = series.getEndIndex();
        
        int calculationIndex = seriesAmountValidator(series.getName(), series, date);
        
        if (calculationIndex < trendPeriod) {
            throw new IllegalArgumentException("Insufficient Data for Trend");
        }

        List<Double> histValues = new ArrayList<>();
        for (int i = calculationIndex - trendPeriod + 1; i <= calculationIndex; i++) {
            double macdValue = macd.getValue(i).doubleValue();
            double signalValue = signal.getValue(i).doubleValue();
            histValues.add(macdValue - signalValue);
        }

        double firstValue = histValues.get(0);
        double lastValue = histValues.get(histValues.size() - 1);
        double slope = (lastValue - firstValue) / trendPeriod;
        
        return slope;
    }
   
    private String detectDivergence(BarSeries series, MACDIndicator macd, int lookback) {
        int endIndex = series.getEndIndex();
        if (endIndex < lookback + 2) {
            return "None";
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);

        List<Integer> localHighs = new ArrayList<>();
        List<Integer> localLows = new ArrayList<>();

        for (int i = endIndex - lookback; i < endIndex; i++) {
            double prev = close.getValue(i - 1).doubleValue();
            double curr = close.getValue(i).doubleValue();
            double next = close.getValue(i + 1).doubleValue();

            if (curr > prev && curr > next) {
                localHighs.add(i);
            } else if (curr < prev && curr < next) {
                localLows.add(i);
            }
        }

        if (localLows.size() >= 2) {
            int lastLow = localLows.get(localLows.size() - 1);
            int prevLow = localLows.get(localLows.size() - 2);

            double priceLast = close.getValue(lastLow).doubleValue();
            double pricePrev = close.getValue(prevLow).doubleValue();

            double macdLast = macd.getValue(lastLow).doubleValue();
            double macdPrev = macd.getValue(prevLow).doubleValue();

            if (priceLast < pricePrev && macdLast > macdPrev) {
                return "Bullish Divergence";
            }
        }

        if (localHighs.size() >= 2) {
            int lastHigh = localHighs.get(localHighs.size() - 1);
            int prevHigh = localHighs.get(localHighs.size() - 2);

            double priceLast = close.getValue(lastHigh).doubleValue();
            double pricePrev = close.getValue(prevHigh).doubleValue();

            double macdLast = macd.getValue(lastHigh).doubleValue();
            double macdPrev = macd.getValue(prevHigh).doubleValue();

            if (priceLast > pricePrev && macdLast < macdPrev) {
                return "Bearish Divergence";
            }
        }

        return "None";
    }

}