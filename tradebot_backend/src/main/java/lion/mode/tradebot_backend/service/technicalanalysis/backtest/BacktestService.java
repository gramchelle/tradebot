package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.dto.indicator.RSIResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.Backtest;
import lion.mode.tradebot_backend.model.Stock;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.service.fetchdata.StockDataService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BacktestService{

    private final RSIService rsiService;
    private final StockDataRepository repository;
    private final BacktestRepository backtestRepository;

    public Backtest rsiHistoricalBacktest(String symbol, int period, int lowerLimit, int upperLimit, LocalDateTime date, int lookback, int lookbackPeriod, String priceType, double calculationConfidence) {
        BarSeries series = loadSeries(symbol);
        Backtest backtest = new Backtest();

        int targetIndex = seriesAmountValidator(symbol, series, date);

        if (targetIndex == -1) throw new NotEnoughDataException("No bar found before or at " + date + " for " + symbol);

        Indicator<Num> prices = priceTypeSelector(priceType, series);

        //initialize variables
        LocalDateTime targetDate = null;
        String signal = "";
        int score = 0;
        int trials = 0;
        double successCount = 0;
        double confidenceWeight = 0;

        System.out.println("---- START RSI BACKTEST ----");

        for (int i = lookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            System.out.println("----");
            System.out.println("Index: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            int rsiScore = rsiService.calculateRSIWithSeries(symbol, period, dateAtIndex, lowerLimit, upperLimit, priceType, series).getScore();

            double price = prices.getValue(index).doubleValue();

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            int rsiScore2 = rsiService.calculateRSIWithSeries(symbol, period, dateAtIndex2, lowerLimit, upperLimit, priceType, series).getScore();

            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            if (rsiScore == 1) {
                if (priceDiff > calculationConfidence) {
                    System.out.println("SUCCESS: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else if (rsiScore == -1) {
                if (priceDiff < -calculationConfidence) {
                    System.out.println("SUCCESS: SELL predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: SELL predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else {
                if (Math.abs(priceDiff) <= calculationConfidence) {
                    System.out.println("SUCCESS: HOLD predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: HOLD predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            }
            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = rsiScore2;
        }

        System.out.println("---- END RSI BACKTEST ----\n");
        if (score == 1){
            System.out.println("For " + targetDate + " the signal is BUY\n");
            signal = "BUY";
        }
        else if (score == -1){
            System.out.println("For " + targetDate + " the signal is SELL\n");
            signal = "SELL";
        }
        else{
            System.out.println("For " + targetDate + " the signal is HOLD\n");
            signal = "HOLD";
        }

        backtest.setSymbol(symbol);
        backtest.setIndicator("rsi");
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setStockDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);

        backtestRepository.save(backtest);

        return backtest;
    }

    // Helper Functions (duplicate) -> TODO: pack them within an utils dir

    private BarSeries loadSeries(String symbol) {

        if (repository.findBySymbol(symbol).isEmpty()) {
            throw new NotEnoughDataException("No data found for symbol: " + symbol);
        }

        List<Stock> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);
        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);

        for (Stock data : dataList) {
            ZonedDateTime endTime = data.getTimestamp().atZone(ZoneId.systemDefault());
            if (series.getBarCount() > 0) {
                ZonedDateTime lastEnd = series.getBar(series.getEndIndex()).getEndTime();
                if (!endTime.isAfter(lastEnd)) {
                    continue;
                }
            }

            Bar bar = BaseBar.builder()
                    .timePeriod(java.time.Duration.ofDays(1)) // [!] The time interval is 1 day
                    .endTime(endTime)
                    .openPrice(DecimalNum.valueOf(data.getOpen()))
                    .highPrice(DecimalNum.valueOf(data.getHigh()))
                    .lowPrice(DecimalNum.valueOf(data.getLow()))
                    .closePrice(DecimalNum.valueOf(data.getClose()))
                    .volume(DecimalNum.valueOf(data.getVolume()))
                    .build();
            series.addBar(bar);
        }
        return series;
    }

    private int seriesAmountValidator(String symbol, BarSeries series, LocalDateTime targetDate){
        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            LocalDateTime barTime = series.getBar(i).getEndTime().toLocalDateTime();
            if (!barTime.isAfter(targetDate)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1) throw new NotEnoughDataException("No bar found before or at " + targetDate + " for " + symbol);
        else return targetIndex;
    }

    private Indicator<Num> priceTypeSelector(String priceType, BarSeries series){
        switch (priceType.toLowerCase()) {
            case "open":
                return new OpenPriceIndicator(series);
            case "close":
                return new ClosePriceIndicator(series);
            case "high":
                return new HighPriceIndicator(series);
            case "low":
                return new LowPriceIndicator(series);
            default:
                throw new IllegalArgumentException("Invalid price type: " + priceType);
        }
    }
}
