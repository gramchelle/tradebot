package lion.mode.tradebot_backend.service.technicalanalysis.indicator;

import lion.mode.tradebot_backend.dto.indicator.TrendlineResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TrendlineService extends IndicatorService {

    public TrendlineService(StockDataRepository repository) {
        super(repository);
    }

    public TrendlineResult calculateTrendline(String symbol, int period, int lookback, LocalDateTime date, double slopeConfidence) {
        BarSeries series = loadSeries(symbol);
        TrendlineResult result = calculateTrendlineWithSeries(symbol, period, date, lookback, series, slopeConfidence);
        return result;
    }

    public TrendlineResult calculateTrendlineWithSeries(String symbol, int period, LocalDateTime date, int lookback, BarSeries series, double slopeConfidence) {
        if (series.getBarCount() < period) throw new NotEnoughDataException("Not enough bars for trendline for " + symbol + " (need period=" + period + ")");

        int targetIndex = seriesAmountValidator(symbol, series, date);

        if (targetIndex < period - 1) throw new NotEnoughDataException("Not enough past bars ending at targetDate for period=" + period);

        int startIndex = targetIndex - period + 1;

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < period; i++) {
            double x = i;
            double y = series.getBar(startIndex + i).getClosePrice().doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        double n = period;
        double denom = n * sumXX - sumX * sumX;
        double slope = denom == 0 ? 0.0 : (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;

        double lastPrice = series.getBar(targetIndex).getClosePrice().doubleValue();
        double slopeRatio = lastPrice == 0 ? 0.0 : slope / lastPrice;

        TrendlineResult result = new TrendlineResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setLookback(lookback);
        result.setSlope(slope);
        result.setIntercept(intercept);
        result.setStartIndex(startIndex);
        result.setDate(date);

        if (slopeRatio > slopeConfidence) {
            result.setDirection("Uptrend");
            result.setSignal("Buy");
            result.setScore(1);
            result.setComment("Trendline indicates an upward trend.");
        } else if (slopeRatio < -slopeConfidence) {
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

        System.out.println("");
        System.out.println("Start Date : " + series.getBar(startIndex).getEndTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        System.out.println("Signal" + result.getSignal());
        System.out.println("Slope : " + slope);
        System.out.println("Slope Ratio : " + slopeRatio);
        System.out.println("Slope Confidence : " + slopeConfidence+"\n");

        checkSupportResistance(series, result, targetIndex, startIndex, lookback);
        return result;
    }

    private void checkSupportResistance(BarSeries series, TrendlineResult result, int endIndex, int startIndex, int lookback) {
        int srStart = Math.max(startIndex, endIndex - lookback);
        int srEnd = endIndex;

        double slope = result.getSlope();
        double intercept = result.getIntercept();
        int baseX = startIndex;

        int touches = 0;
        double tolPercent = 0.01;

        for (int j = srStart; j <= srEnd; j++) {
            int relX = j - baseX;
            double expected = intercept + slope * relX;
            double actual = series.getBar(j).getClosePrice().doubleValue();
            double tolerance = Math.max(1e-8, Math.abs(expected) * tolPercent);

            if (Math.abs(expected - actual) <= tolerance) {
                touches++;
            }
        }

        if (touches >= 2) {
            if (slope > 0) result.setActsAsResistance(true);
            else if (slope < 0) result.setActsAsSupport(true);
        }
    }

}
