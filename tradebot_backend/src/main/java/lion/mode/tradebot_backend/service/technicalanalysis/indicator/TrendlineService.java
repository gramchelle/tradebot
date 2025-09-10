package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.indicator.TrendlineResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.time.LocalDateTime;

@Service
public class TrendlineService extends IndicatorService {

    public TrendlineService(StockDataRepository repository) {
        super(repository);
    }

    public TrendlineResult calculateTrendline(String symbol, int period, int lookback, LocalDateTime date) {
        BarSeries series = loadSeries(symbol);
        return calculateTrendlineWithSeries(symbol, period, lookback, series);
    }

    public TrendlineResult calculateTrendlineWithSeries(String symbol, int period, int lookback, BarSeries series) {
        if (series.getBarCount() < period + lookback) throw new NotEnoughDataException("Not enough data for Trendline for " + symbol);

        int targetIndex = series.getEndIndex();

        double slope = computeSlope(series, period, targetIndex);

        TrendlineResult result = buildResult(symbol, period, lookback, slope, series.getBar(targetIndex).getEndTime().toLocalDateTime());
        checkSupportResistance(series, result, targetIndex, period, lookback);
        return result;
    }

    private double computeSlope(BarSeries series, int period, int endIndex) {
        int startIndex = endIndex - period + 1;

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < period; i++) {
            int x = i;
            double y = series.getBar(startIndex + i).getClosePrice().doubleValue();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        return (period * sumXY - sumX * sumY) / (period * sumXX - sumX * sumX);
    }

    private TrendlineResult buildResult(String symbol, int period, int lookback, double slope, LocalDateTime date) {
        TrendlineResult result = new TrendlineResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setLookback(lookback);
        result.setSlope(slope);
        result.setDate(date);

        if (slope > 5.0) {
            result.setDirection("Uptrend");
            result.setSignal("Buy");
            result.setScore(1);
            result.setComment("Trendline indicates an upward trend.");
        } else if (slope < -5.0) {
            result.setDirection("Downtrend");
            result.setSignal("Sell");
            result.setScore(-1);
            result.setComment("Trendline indicates a downward trend.");
        } else {
            result.setDirection("Sideways");
            result.setSignal("Hold");
            result.setScore(0);
            result.setComment("Trendline is flat (sideways market).");
        }
        return result;
    }

    // TODO: refine logic
    private void checkSupportResistance(BarSeries series, TrendlineResult result, int endIndex, int period, int lookback) {
        int startIndex = Math.max(0, endIndex - lookback);
        double intercept = series.getBar(endIndex).getClosePrice().doubleValue() - result.getSlope() * (period - 1);

        int touches = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            double expected = intercept + result.getSlope() * i;
            double actual = series.getBar(i).getClosePrice().doubleValue();
            double tolerance = expected * 0.01; // error tolerance
            if (Math.abs(expected - actual) <= tolerance) {
                touches++;
            }
        }

        if (touches >= 2) {
            if ("Uptrend".equals(result.getDirection())) {
                result.setActsAsSupport(true);
            }
            if ("Downtrend".equals(result.getDirection())) {
                result.setActsAsResistance(true);
            }
        }
            
    }
}
