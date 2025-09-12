package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.Backtest;
import lion.mode.tradebot_backend.model.Stock;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
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
    private final MACrossService maCrossoverService;
    private final StockDataRepository repository;
    private final BacktestRepository backtestRepository;
    private final MACDService macdService;
    private final BollingerBandsService bollingerBandsService;
    private final DMIService dmiService;
    private final MFIService mfiService;
    private final TrendlineService trendlineService;

    // initialize variables for backtest
    private LocalDateTime targetDate = null;
    private String signal = "";
    private int score = 0;
    private int trials = 0;
    private double successCount = 0;
    private double confidenceWeight = 0;

    // Main Backtest Logic Methods

    public Backtest rsiHistoricalBacktest(String symbol, int period, int lowerLimit, int upperLimit, LocalDateTime date, int lookback, int horizon, String priceType, double calculationConfidence) {
        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = priceTypeSelector(priceType, series);

        System.out.println("---- START RSI BACKTEST ----");

        for (int i = lookback; i > 0; i -= horizon) {
            int index = targetIndex - i;
            if (index < 0) break;

            int index2 = index + horizon;
            if (index2 >= series.getBarCount()) break;

            System.out.println("----\nIndex: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();

            int rsiScore = rsiService.calculateRSIWithSeries(symbol, period, dateAtIndex, lowerLimit, upperLimit, priceType, series).getScore();
            int rsiScore2 = rsiService.calculateRSIWithSeries(symbol, period, dateAtIndex2, lowerLimit, upperLimit, priceType, series).getScore();
 
            double price = prices.getValue(index).doubleValue();
            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            successCount = getSuccessCount(rsiScore, priceDiff, dateAtIndex, dateAtIndex2, calculationConfidence);

            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = rsiScore2;
        }

        System.out.println("---- END RSI BACKTEST ----\n");
        
        signal = setSignal(score, targetDate, confidenceWeight);

        Backtest backtest = saveBacktestResult(symbol, "rsi", signal, score, targetDate, confidenceWeight);
        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest emaCrossoverHistoricalBacktest(String symbol, int shortPeriod, int longPeriod, int lookback, LocalDateTime date, String priceType, int backtestLookback, int horizon, double calculationConfidence){
        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = priceTypeSelector(priceType, series);

        System.out.println("---- START EMA CROSSOVER BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= horizon) {
            int index = targetIndex - i;
            if (index < 0) break;

            int index2 = index + horizon;
            if (index2 >= series.getBarCount()) break;

            System.out.println("-----\nIndex: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();

            int emaCrossoverScore = maCrossoverService.calculateEMACrossWithSeries(symbol, shortPeriod, longPeriod, dateAtIndex, lookback, priceType, series).getScore();
            int emaCrossoverScore2 = maCrossoverService.calculateEMACrossWithSeries(symbol, shortPeriod, longPeriod, dateAtIndex2, lookback, priceType, series).getScore();

            double price = prices.getValue(index).doubleValue();
            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            successCount = getSuccessCount(emaCrossoverScore, priceDiff, dateAtIndex, dateAtIndex2, calculationConfidence);

            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = emaCrossoverScore2;
        }

        System.out.println("---- END EMA BACKTEST ----\n");

        signal = setSignal(score, targetDate, confidenceWeight);

        Backtest backtest = saveBacktestResult(symbol, "ema", signal, score, targetDate, confidenceWeight);
        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest smaCrossoverHistoricalBacktest(String symbol, int shortPeriod, int longPeriod, int lookback, LocalDateTime date, String priceType, int backtestLookback, int horizon, double calculationConfidence){
        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = priceTypeSelector(priceType, series);

        System.out.println("---- START SMA CROSSOVER BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= horizon) {
            int index = targetIndex - i;
            if (index < 0) break;

            int index2 = index + horizon;
            if (index2 >= series.getBarCount()) break;

            System.out.println("-----\nIndex: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();

            int smaCrossoverScore = maCrossoverService.calculateSMACrossWithSeries(symbol, shortPeriod, longPeriod, dateAtIndex, lookback, priceType, series).getScore();
            int smaCrossoverScore2 = maCrossoverService.calculateSMACrossWithSeries(symbol, shortPeriod, longPeriod, dateAtIndex2, lookback, priceType, series).getScore();
        
            double price = prices.getValue(index).doubleValue();
            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            successCount = getSuccessCount(smaCrossoverScore, priceDiff, dateAtIndex, dateAtIndex2, calculationConfidence);

            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = smaCrossoverScore2;
        }

        System.out.println("---- END SMA CROSSOVER BACKTEST ----\n");

        signal = setSignal(score, targetDate, confidenceWeight);

        Backtest backtest = saveBacktestResult(symbol, "sma", signal, score, targetDate, confidenceWeight);
        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest macdHistoricalBacktest(String symbol, int shortPeriod, int longPeriod, int signalPeriod, LocalDateTime date, String priceType, int backtestLookback, int horizon, double calculationConfidence){
        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = priceTypeSelector(priceType, series);

        System.out.println("---- START MACD BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= horizon) {
            int index = targetIndex - i;
            if (index < 0) break;

            int index2 = index + horizon;
            if (index2 >= series.getBarCount()) break;

            System.out.println("-----\nIndex: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();

            int macdScore = macdService.calculateMacdWithSeries(symbol, shortPeriod, longPeriod, signalPeriod, dateAtIndex, 2, 1.0, priceType, series).getScore();
            int macdScore2 = macdService.calculateMacdWithSeries(symbol, shortPeriod, longPeriod, signalPeriod, dateAtIndex2, 2, 1.0, priceType, series).getScore();

            double price = prices.getValue(index).doubleValue();
            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            successCount = getSuccessCount(macdScore, priceDiff, dateAtIndex, dateAtIndex2, calculationConfidence);

            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = macdScore2;
        }

        System.out.println("---- END MACD BACKTEST ----\n");

        signal = setSignal(score, targetDate, confidenceWeight);

        Backtest backtest = saveBacktestResult(symbol, "MACD", signal, score, targetDate, confidenceWeight);
        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest bollingerHistoricalBacktest(String symbol, int period, double nbDev, LocalDateTime date, double squeezeConfidence, String priceType, int backtestLookback, int horizon, double calculationConfidence){
        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = priceTypeSelector(priceType, series);

        System.out.println("---- START BOLLINGER BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= horizon) {
            int index = targetIndex - i;
            if (index < 0) break;

            int index2 = index + horizon;
            if (index2 >= series.getBarCount()) break;

            System.out.println("----\nIndex: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();

            int bollingerScore = bollingerBandsService.calculateBollingerWithSeries(symbol, period, nbDev, dateAtIndex, squeezeConfidence, priceType, series).getScore();
            int bollingerScore2 = bollingerBandsService.calculateBollingerWithSeries(symbol, period, nbDev, dateAtIndex2, squeezeConfidence, priceType, series).getScore();

            double price = prices.getValue(index).doubleValue();
            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            successCount = getSuccessCount(bollingerScore, priceDiff, dateAtIndex, dateAtIndex2, calculationConfidence);

            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = bollingerScore2;
        }

        System.out.println("---- END BOLLINGER BACKTEST ----\n");

        signal = setSignal(score, targetDate, confidenceWeight);

        Backtest backtest = saveBacktestResult(symbol, "Bollinger Bands", signal, score, targetDate, confidenceWeight);
        backtestRepository.save(backtest);

        return backtest;
    }
    
    public Backtest dmiHistoricalBacktest(String symbol, int period, LocalDateTime date, String priceType, int backtestLookback, int horizon, double calculationConfidence){
        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = priceTypeSelector(priceType, series);

        System.out.println("---- START DMI BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= horizon) {
            int index = targetIndex - i;
            if (index < 0) break;

            int index2 = index + horizon;
            if (index2 >= series.getBarCount()) break;

            System.out.println("-----\nIndex: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();

            int dmiScore = dmiService.calculateDmiWithSeries(symbol, period, dateAtIndex, series).getScore();
            int dmiScore2 = dmiService.calculateDmiWithSeries(symbol, period, dateAtIndex2, series).getScore();

            double price = prices.getValue(index).doubleValue();
            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            successCount = getSuccessCount(dmiScore, priceDiff, dateAtIndex, dateAtIndex2, calculationConfidence);

            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = dmiScore2;
        }

        System.out.println("---- END DMI BACKTEST ----\n");

        signal = setSignal(score, targetDate, confidenceWeight);

        Backtest backtest = saveBacktestResult(symbol, "DMI", signal, score, targetDate, confidenceWeight);
        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest mfiHistoricalBacktest(String symbol, int period, LocalDateTime date, int lowerLimit, int upperLimit, String priceType, int backtestLookback, int lookbackPeriod, double calculationConfidence){
        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = priceTypeSelector(priceType, series);

        System.out.println("---- START MFI BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            System.out.println("-----\nIndex: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();

            int mfiScore = mfiService.calculateMfiWithSeries(symbol, period, dateAtIndex, lowerLimit, upperLimit, series).getScore();
            int mfiScore2 = mfiService.calculateMfiWithSeries(symbol, period, dateAtIndex2, lowerLimit, upperLimit, series).getScore();

            double price = prices.getValue(index).doubleValue();
            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            successCount = getSuccessCount(mfiScore, priceDiff, dateAtIndex, dateAtIndex2, calculationConfidence);
            
            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = mfiScore2;
        }

        System.out.println("---- END MFI BACKTEST ----\n");

        signal = setSignal(score, targetDate, confidenceWeight);

        Backtest backtest = saveBacktestResult(symbol, "mfi", signal, score, targetDate, confidenceWeight);
        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest trendlineHistoricalBacktest(String symbol, int period, LocalDateTime date, int lookback, double slopeConfidence, String priceType, int backtestLookback, int horizon, double calculationConfidence){
        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);
        Indicator<Num> prices = priceTypeSelector(priceType, series);

        System.out.println("---- START TRENDLINE BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= horizon) {
            int index = targetIndex - i;
            if (index < 0) break;

            int index2 = index + horizon;
            if (index2 >= series.getBarCount()) break;

            System.out.println("-----\nIndex: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();

            int trendlineScore = trendlineService.calculateTrendlineWithSeries(symbol, period, dateAtIndex, lookback, series, slopeConfidence).getScore();
            int trendlineScore2 = trendlineService.calculateTrendlineWithSeries(symbol, period, dateAtIndex2, lookback, series, slopeConfidence).getScore();

            double price = prices.getValue(index).doubleValue();
            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            successCount = getSuccessCount(trendlineScore, priceDiff, dateAtIndex, dateAtIndex2, calculationConfidence);

            trials ++;
            confidenceWeight = successCount / trials; // check divide by zero exception
            System.out.println("- Confidence Weight " + confidenceWeight + "\n");
            targetDate = dateAtIndex2;
            score = trendlineScore2;
        }

        System.out.println("---- END TRENDLINE BACKTEST ----\n");

        signal = setSignal(score, targetDate, confidenceWeight);

        Backtest backtest = saveBacktestResult(symbol, "trendline", signal, score, targetDate, confidenceWeight);
        backtestRepository.save(backtest);

        return backtest;
    }
    
    // Common Functions

    private Backtest saveBacktestResult(String symbol, String indicator, String signal, int score, LocalDateTime targetDate, double confidenceWeight){
        Backtest backtest = new Backtest();
        backtest.setSymbol(symbol);
        backtest.setIndicator(indicator);
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setStockDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);
        return backtest;
    }

    private String setSignal(int score, LocalDateTime targetDate, double confidenceWeight){
        String signal;
        if (score == 1){
            System.out.println("[RESULT] For " + targetDate + " the signal is BUY with confidence weight " + confidenceWeight + "\n");
            signal = "BUY";
        }
        else if (score == -1){
            System.out.println("[RESULT] For " + targetDate + " the signal is SELL with confidence weight " + confidenceWeight + "\n");
            signal = "SELL";
        }
        else{
            System.out.println("[RESULT] For " + targetDate + " the signal is HOLD with confidence weight " + confidenceWeight + "\n");
            signal = "HOLD";
        }
        return signal;
    }

    private double getSuccessCount(int score, double priceDiff, LocalDateTime dateAtIndex, LocalDateTime dateAtIndex2, double calculationConfidence){
        if (score == 1) {
            if (priceDiff > calculationConfidence) {
                System.out.println("[SUCCESS] BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                successCount++;
            } else
                System.out.println("[VIOLENCE] BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
        } else if (score == -1) {
            if (priceDiff < -calculationConfidence) {
                System.out.println("[SUCCESS] SELL predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                successCount++;
            } else
                System.out.println("[VIOLENCE] SELL predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
        } else {
            if (Math.abs(priceDiff) <= calculationConfidence) {
                System.out.println("[SUCCESS] HOLD predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                successCount++;
            } else
                System.out.println("[VIOLENCE] HOLD predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
        }
        return successCount;
    }

    // Helper Functions

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
