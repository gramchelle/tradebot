package lion.mode.tradebot_backend.service.technicalanalysis;

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
public class RsiService extends IndicatorService {

    public RsiService(StockDataRepository repository) {
        super(repository);
    }

    public RSIResult calculateRSI(String symbol, int period, int trendPeriod) {
        BarSeries series = loadSeries(symbol);
        if (series.getBarCount() < period) {
            throw new NotEnoughDataException("Not enough data for RSI calculation for " + symbol);
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(close, period);

        int lastIndex = series.getEndIndex();
        double lastRsi = rsi.getValue(lastIndex).doubleValue();
        double prevRsi = rsi.getValue(lastIndex - trendPeriod).doubleValue();

        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setRsiValue(lastRsi);
        result.setDate(series.getLastBar().getEndTime().toLocalDateTime());
        result.setTrendPeriod(trendPeriod);

        generateSignalAndScoreResult(result, lastRsi);
        if (lastRsi > prevRsi) {
            result.setTrendComment("RSI Trend is increasing by " + (lastRsi - prevRsi) + " since " + trendPeriod + " period");
        } else {
            result.setTrendComment("RSI Trend is decreasing by " + (prevRsi - lastRsi) +  " since " + trendPeriod + " period");
        }
        result.setDivergence("none");

        return result;
    }

    private void generateSignalAndScoreResult(RSIResult result, double rsiValue){
        if (rsiValue > 68) {
            result.setSignal("Sell");
            result.setScore(-1);
        } else if (rsiValue < 32) {
            result.setSignal("Buy");
            result.setScore(1);
        } else {
            result.setSignal("Hold");
            result.setScore(0);
        }
    }

    public RSIResult calculateRSI(String symbol, int period, LocalDateTime targetDate, int trendPeriod) {
        BarSeries series = loadSeries(symbol);

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
        double prevRsi = rsi.getValue(targetIndex - trendPeriod).doubleValue();

        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setRsiValue(rsiValue);
        result.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());

        generateSignalAndScoreResult(result, rsiValue);

        if (rsiValue > prevRsi) {
            result.setTrendComment("RSI Trend is increasing by " + (rsiValue - prevRsi));
        } else if (rsiValue < prevRsi) {
            result.setTrendComment("RSI Trend is decreasing by " + (prevRsi - rsiValue));
        } else {
            result.setTrendComment("RSI Trend is going sideways.");
        }

        result.setDivergence("none"); //TODO: Add divergence detection

        return result;
    }

    private Duration detectBarDuration(List<StockData> dataList) {
        if (dataList.size() < 2) return Duration.ofHours(1);
        LocalDateTime t0 = dataList.get(0).getTimestamp();
        LocalDateTime t1 = dataList.get(1).getTimestamp();
        return Duration.between(t0, t1);
    }

    //TODO: Enhance this method
    private String detectDivergence(BarSeries series, int period) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, period);

        int endIndex = series.getEndIndex();
        int lookback = Math.min(period * 3, endIndex);
        int startIndex = Math.max(1, endIndex - lookback);

        List<Integer> lowIndices = new ArrayList<>();
        List<Integer> highIndices = new ArrayList<>();

        for (int i = startIndex; i <= endIndex - 1; i++) {
            double price = closePrice.getValue(i).doubleValue();
            double prevPrice = closePrice.getValue(i - 1).doubleValue();
            double nextPrice = closePrice.getValue(i + 1).doubleValue();

            if (price < prevPrice && price < nextPrice) lowIndices.add(i);
            if (price > prevPrice && price > nextPrice) highIndices.add(i);
        }

        // Bullish divergence
        if (lowIndices.size() >= 2) {
            int i1 = lowIndices.get(lowIndices.size() - 2);
            int i2 = lowIndices.get(lowIndices.size() - 1);
            if (closePrice.getValue(i2).doubleValue() < closePrice.getValue(i1).doubleValue() &&
                rsi.getValue(i2).doubleValue() > rsi.getValue(i1).doubleValue()) {
                return "bullish";
            }
        }

        // Bearish divergence
        if (highIndices.size() >= 2) {
            int i1 = highIndices.get(highIndices.size() - 2);
            int i2 = highIndices.get(highIndices.size() - 1);
            if (closePrice.getValue(i2).doubleValue() > closePrice.getValue(i1).doubleValue() &&
                rsi.getValue(i2).doubleValue() < rsi.getValue(i1).doubleValue()) {
                return "bearish";
            }
        }

        return "none";
    }
}
