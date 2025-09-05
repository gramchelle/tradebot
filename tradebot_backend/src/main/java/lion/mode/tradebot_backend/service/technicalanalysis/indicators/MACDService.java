package lion.mode.tradebot_backend.service.technicalanalysis.indicators;

import lion.mode.tradebot_backend.dto.indicators.MACDResult;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MACDService extends IndicatorService {

    public MACDService(StockDataRepository repository) {
        super(repository);
    }

    public MACDResult calculateMacd(String symbol, int shortPeriod, int longPeriod, int signalPeriod, LocalDateTime targetDateTime, int histogramTrendPeriod, double histogramConfidence) {
        BarSeries series = loadSeries(symbol);

        if (series == null || series.isEmpty()) {
            throw new IllegalArgumentException("No data available for symbol: " + symbol);
        }

        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            Bar bar = series.getBar(i);
            LocalDateTime barTime = bar.getEndTime().toLocalDateTime();
            if (!barTime.isAfter(targetDateTime)) {
                targetIndex = i;
            } else {
                break;
            }
        }

        if (targetIndex == -1) {
            throw new IllegalArgumentException("No bar found before or at " + targetDateTime);
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, shortPeriod, longPeriod);
        EMAIndicator signal = new EMAIndicator(macd, signalPeriod);

        MACDResult result = new MACDResult();
        result.setSymbol(symbol);
        result.setShortPeriod(shortPeriod);
        result.setLongPeriod(longPeriod);
        result.setSignalPeriod(signalPeriod);

        result.setMacdScore(macd.getValue(targetIndex).doubleValue());
        result.setSignalScore(signal.getValue(targetIndex).doubleValue());
        result.setHistogramValue(result.getMacdScore() - result.getSignalScore());

        if (result.getHistogramValue() > 0) result.setMaCross("Bullish");
        else result.setMaCross("Bearish");

        result.setHistogramTrendPeriod(histogramTrendPeriod);
        result.setHistogramTrend(detectHistogramTrend(series, macd, signal, histogramTrendPeriod, histogramConfidence));
        result.setDivergence(detectDivergence(series, macd, longPeriod));

        if (result.getMaCross().equals("Bullish") && result.getHistogramTrend().equals("Increasing")) {
            result.setSignal("Sell");
            result.setScore(-1);
        } else if (result.getMaCross().equals("Bullish") && result.getHistogramTrend().equals("Decreasing")) {
            result.setSignal("Hold");
            result.setScore(0);
        } else if (result.getMaCross().equals("Bearish") && result.getHistogramTrend().equals("Decreasing")) {
            result.setSignal("Buy");
            result.setScore(1);
        } else if (result.getMaCross().equals("Bearish") && result.getHistogramTrend().equals("Increasing")) {
            result.setSignal("Hold");
            result.setScore(0);
        } else if (result.getMaCross().equals("Bearish") && result.getHistogramTrend().equals("Sideways")) {
            result.setSignal("Buy");
            result.setScore(1);
        } else {
            result.setSignal("Hold");
            result.setScore(0);
        }

        return result;
    }

    private String detectHistogramTrend(BarSeries series, MACDIndicator macd, EMAIndicator signal, int trendPeriod, double histogramConfidence) {
        int endIndex = series.getEndIndex();
        if (endIndex < trendPeriod) {
            return "Not enough data";
        }

        List<Double> histValues = new ArrayList<>();
        for (int i = endIndex - trendPeriod + 1; i <= endIndex; i++) {
            double macdValue = macd.getValue(i).doubleValue();
            double signalValue = signal.getValue(i).doubleValue();
            histValues.add(macdValue - signalValue);
        }

        double first = histValues.get(0);
        double last = histValues.get(histValues.size() - 1);

        first += histogramConfidence;

        if (last > first) {
            return "Increasing";
        } else if (last < first) {
            return "Decreasing";
        } else {
            return "Sideways";
        }
    }

    // TODO: Enhance this method
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
