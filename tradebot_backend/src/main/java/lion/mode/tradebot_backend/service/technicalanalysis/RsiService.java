package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.dto.indicators.rsi.RSIResult;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class RsiService extends IndicatorService {

    public RsiService(StockDataRepository repository) {
        super(repository);
    }

    public RSIResult calculateRSI(String symbol, int period) {
        BarSeries series = loadSeries(symbol);
        if (series.getBarCount() < period) {
            throw new NotEnoughDataException("Not enough data for RSI calculation for " + symbol);
        }

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(close, period);

        int lastIndex = series.getEndIndex();
        double lastRsi = rsi.getValue(lastIndex).doubleValue();
        double prevRsi = rsi.getValue(lastIndex - 1).doubleValue();

        RSIResult result = new RSIResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setRsiValue(lastRsi);
        result.setDate(series.getLastBar().getEndTime().toLocalDateTime());
        generateSignalAndScoreResult(result, lastRsi);
        if (lastRsi > prevRsi) {
            result.setTrendComment("RSI Trend is increasing by " + (lastRsi - prevRsi));
        } else {
            result.setTrendComment("RSI Trend is decreasing by " + (prevRsi - lastRsi));
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

    public RSIResult calculateRSI(String symbol, int period, LocalDateTime targetDate) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < period + 1) {
            throw new NotEnoughDataException("Not enough data for RSI at " + targetDate + " for " + symbol);
        }

        // Target index bulma (MACD'deki gibi)
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
        double prevRsi = rsi.getValue(targetIndex - 1).doubleValue();

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
            result.setTrendComment("RSI Trend is stable");
        }

        result.setDivergence("none"); // ileride divergence detection eklersin

        return result;
    }

    public RSIResult calculateRSIWithDivergence(String symbol, int period) {
        RSIResult rsiResult = calculateRSI(symbol, period);
        BarSeries series = loadSeries(symbol);
        rsiResult.setDivergence(detectDivergence(series, period));
        return rsiResult;
    }

    public RSIResult calculateRSIWithDivergence(String symbol, int period, LocalDateTime targetDate) {
        RSIResult rsiResult = calculateRSI(symbol, period, targetDate);

        List<StockData> dataList = repository.findBySymbolAndTimestampLessThanEqualOrderByTimestampAsc(symbol, targetDate);
        Duration barDuration = detectBarDuration(dataList);

        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);
        for (StockData data : dataList) {
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.of("America/New_York"));
            Bar bar = BaseBar.builder()
                    .timePeriod(barDuration)
                    .endTime(endTime)
                    .openPrice(DecimalNum.valueOf(data.getOpen()))
                    .highPrice(DecimalNum.valueOf(data.getHigh()))
                    .lowPrice(DecimalNum.valueOf(data.getLow()))
                    .closePrice(DecimalNum.valueOf(data.getClose()))
                    .volume(DecimalNum.valueOf(data.getVolume()))
                    .build();
            series.addBar(bar);
        }

        rsiResult.setDivergence(detectDivergence(series, period));
        return rsiResult;
    }

    private Duration detectBarDuration(List<StockData> dataList) {
        if (dataList.size() < 2) return Duration.ofHours(1);
        LocalDateTime t0 = dataList.get(0).getTimestamp();
        LocalDateTime t1 = dataList.get(1).getTimestamp();
        return Duration.between(t0, t1);
    }

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
