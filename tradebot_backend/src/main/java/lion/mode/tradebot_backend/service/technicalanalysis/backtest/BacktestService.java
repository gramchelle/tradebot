package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.*;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.Backtest;
import lion.mode.tradebot_backend.model.StockDataDaily;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BacktestService{

    private final StockDataRepository repository;
    private final RSIService rsiService;
    private final BacktestRepository backtestRepository;
//    private final MACrossoverService maCrossoverService;
//    private final MACDService macdService;
//    private final BollingerBandsService bollingerBandsService;
//    private final DMIService dmiService;
//    private final MFIService mfiService;
//    private final TrendlineService trendlineService;


    public Backtest rsiHistoricalBacktest(RSIEntry rsiEntry, int lookback, int horizon, double calculationConfidence){
        // extract / defaults from entry
        String symbol = rsiEntry.getSymbol();
        LocalDateTime targetDate = rsiEntry.getDate() != null ? rsiEntry.getDate() : LocalDateTime.now();
        int period = rsiEntry.getPeriod() > 0 ? rsiEntry.getPeriod() : 14;
        int lowerLimit = rsiEntry.getLowerLimit() > 0 ? rsiEntry.getLowerLimit() : 30;
        int upperLimit = rsiEntry.getUpperLimit() > 0 ? rsiEntry.getUpperLimit() : 70;
        String priceType = rsiEntry.getSource() != null ? rsiEntry.getSource() : "close";

        BarSeries series = loadSeries(symbol);
        Backtest backtest = new Backtest();

        int targetIndex = seriesAmountValidator(symbol, series, targetDate);

        Indicator<Num> prices = sourceSelector(priceType, series);

        // accumulators - init vars
        int trials = 0;
        int successCount = 0;
        int failCount = 0;

        int buyTrials  = 0, buySuccess  = 0;
        int sellTrials = 0, sellSuccess = 0;
        int holdTrials = 0, holdSuccess = 0;

        List<Double> priceDiffs = new ArrayList<>();
        LocalDateTime firstTestDate = null;
        LocalDateTime lastTestDate = null;

        for (int i = lookback; i > 0; i -= horizon) {
            int index = targetIndex - i;
            if (index < 0) break;

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();

            // build a per-iteration RSIEntry (reuse base entry values but override date)
            RSIEntry entry = new RSIEntry();
            entry.setSymbol(symbol);
            entry.setDate(dateAtIndex);
            entry.setPeriod(period);
            entry.setLowerLimit(lowerLimit);
            entry.setUpperLimit(upperLimit);
            entry.setSource(priceType);

            // predicted response at index (uses your RSIService implementation)
            BaseIndicatorResponse r1 = rsiService.calculateWithSeries(entry, series);
            double predictedScore = r1.getScore(); // your service's score (granular)
            double price = prices.getValue(index).doubleValue();

            int index2 = index + horizon;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            double price2 = prices.getValue(index2).doubleValue();

            // price return relative to entry price
            double priceDiff = (price2 - price) / price;
            priceDiffs.add(priceDiff);

            // normalize predictedScore to simple -1/0/1
            int simplePred;
            if (predictedScore > 0) simplePred = 1;
            else if (predictedScore < 0) simplePred = -1;
            else simplePred = 0;

            boolean success = false;
            if (simplePred == 1) {
                buyTrials++;
                if (priceDiff > calculationConfidence) {
                    buySuccess++;
                    success = true;
                }
            } else if (simplePred == -1) {
                sellTrials++;
                if (priceDiff < -calculationConfidence) {
                    sellSuccess++;
                    success = true;
                }
            } else {
                holdTrials++;
                if (Math.abs(priceDiff) <= calculationConfidence) {
                    holdSuccess++;
                    success = true;
                }
            }

            trials++;
            if (success) successCount++; else failCount++;

            if (firstTestDate == null) firstTestDate = dateAtIndex;
            lastTestDate = dateAtIndex2;

            System.out.println("Index: " + index + " priceDiff: " + priceDiff);
            System.out.println("Success: " + successCount + " Fail: " + failCount + "\n");
        }

        // aggregate stats
        double averagePriceMovement = 0.0;
        double maxPriceMovement = Double.NEGATIVE_INFINITY;
        double minPriceMovement = Double.POSITIVE_INFINITY;
        double volatility = 0.0;

        if (!priceDiffs.isEmpty()) {
            double sum = 0.0;
            for (double d : priceDiffs) {
                sum += d;
                if (d > maxPriceMovement) maxPriceMovement = d;
                if (d < minPriceMovement) minPriceMovement = d;
            }
            averagePriceMovement = sum / priceDiffs.size();

            double mean = averagePriceMovement;
            double sqSum = 0.0;
            for (double d : priceDiffs) {
                sqSum += (d - mean) * (d - mean);
            }
            volatility = Math.sqrt(sqSum / priceDiffs.size());
        } else {
            maxPriceMovement = 0;
            minPriceMovement = 0;
        }

        double confidenceWeight = trials > 0 ? ((double) successCount / trials) : 0.0;

        // final decision: re-evaluate latest point (optional improvement possible)
        double finalScore = 0;
        String finalSignal = "HOLD";
        if (lastTestDate != null) {
            RSIEntry lastEntry = new RSIEntry();
            lastEntry.setSymbol(symbol);
            lastEntry.setDate(lastTestDate);
            lastEntry.setPeriod(period);
            lastEntry.setLowerLimit(lowerLimit);
            lastEntry.setUpperLimit(upperLimit);
            lastEntry.setSource(priceType);
            BaseIndicatorResponse lastR = rsiService.calculateWithSeries(lastEntry, series);
            finalScore = lastR.getScore();
            if (finalScore > 0) finalSignal = "BUY";
            else if (finalScore < 0) finalSignal = "SELL";
        }

        // persist to Backtest entity
        backtest.setSymbol(symbol);
        backtest.setIndicator("RSI");
        backtest.setSignal(finalSignal);
        backtest.setScore(finalScore);
        backtest.setDate(lastTestDate);
        backtest.setConfidenceScore(confidenceWeight);
        backtest.setTotalTrials(trials);
        backtest.setSuccessfulPredictions(successCount);
        backtest.setFailedPredictions(failCount);
        backtest.setLookback(lookback);
        backtest.setLookbackPeriod(horizon);
        backtest.setCalculationConfidence(calculationConfidence);
        backtest.setPriceType(priceType);

        // optional: record used indicator parameters as map/json
        Map<String, Object> params = Map.of(
                "period", period,
                "lowerLimit", lowerLimit,
                "upperLimit", upperLimit
        );
        backtest.setIndicatorParameters(params);

        backtest.setAveragePriceMovement(averagePriceMovement);
        backtest.setMaxPriceMovement(maxPriceMovement);
        backtest.setMinPriceMovement(minPriceMovement);
        backtest.setVolatility(volatility);
        backtest.setBacktestStartDate(firstTestDate);
        backtest.setBacktestEndDate(lastTestDate);
        backtest.setStatus("SUCCESS");

        backtestRepository.save(backtest);
        return backtest;
    }

/*
    public Backtest emaCrossoverHistoricalBacktest(String symbol, int shortPeriod, int longPeriod, int lookback, LocalDateTime date, String priceType, int backtestLookback, int lookbackPeriod, double calculationConfidence){
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

        System.out.println("---- START EMA CROSSOVER BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            System.out.println("----");
            System.out.println("Index: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            int emaCrossoverScore = maCrossoverService.calculateEMACrossWithSeries(symbol, shortPeriod, longPeriod, dateAtIndex, lookback, priceType, series).getScore();

            double price = prices.getValue(index).doubleValue();

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            int emaCrossoverScore2 = maCrossoverService.calculateEMACrossWithSeries(symbol, shortPeriod, longPeriod, dateAtIndex2, lookback, priceType, series).getScore();

            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            if (emaCrossoverScore == 1) {
                if (priceDiff > calculationConfidence) {
                    System.out.println("SUCCESS: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else if (emaCrossoverScore == -1) {
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
            score = emaCrossoverScore2;
        }

        System.out.println("---- END EMA BACKTEST ----\n");
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
        backtest.setIndicator("EMA");
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);

        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest smaCrossoverHistoricalBacktest(String symbol, int shortPeriod, int longPeriod, int lookback, LocalDateTime date, String priceType, int backtestLookback, int lookbackPeriod, double calculationConfidence){
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

        System.out.println("---- START SMA CROSSOVER BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            System.out.println("----");
            System.out.println("Index: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            int smaCrossoverScore = maCrossoverService.calculateSMACrossWithSeries(symbol, shortPeriod, longPeriod, dateAtIndex, lookback, priceType, series).getScore();

            double price = prices.getValue(index).doubleValue();

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            int smaCrossoverScore2 = maCrossoverService.calculateSMACrossWithSeries(symbol, shortPeriod, longPeriod, dateAtIndex2, lookback, priceType, series).getScore();

            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            if (smaCrossoverScore == 1) {
                if (priceDiff > calculationConfidence) {
                    System.out.println("SUCCESS: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else if (smaCrossoverScore == -1) {
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
            score = smaCrossoverScore2;
        }

        System.out.println("---- END SMA CROSSOVER BACKTEST ----\n");
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
        backtest.setIndicator("SMA");
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);

        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest macdHistoricalBacktest(String symbol, int shortPeriod, int longPeriod, int signalPeriod, LocalDateTime date, String priceType, int backtestLookback, int lookbackPeriod, double calculationConfidence){
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

        System.out.println("---- START MACD BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            System.out.println("----");
            System.out.println("Index: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            int macdScore = macdService.calculateMacdWithSeries(symbol, shortPeriod, longPeriod, signalPeriod, dateAtIndex, 2, 1.0, priceType, series).getScore();

            double price = prices.getValue(index).doubleValue();

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            int macdScore2 = macdService.calculateMacdWithSeries(symbol, shortPeriod, longPeriod, signalPeriod, dateAtIndex2, 2, 1.0, priceType, series).getScore();

            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            if (macdScore == 1) {
                if (priceDiff > calculationConfidence) {
                    System.out.println("SUCCESS: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else if (macdScore == -1) {
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
            score = macdScore2;
        }

        System.out.println("---- END MACD BACKTEST ----\n");
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
        backtest.setIndicator("MACD");
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);

        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest bollingerHistoricalBacktest(String symbol, int period, double nbDev, LocalDateTime date, double squeezeConfidence, String priceType, int backtestLookback, int lookbackPeriod, double calculationConfidence){
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

        System.out.println("---- START BOLLINGER BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            System.out.println("----");
            System.out.println("Index: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            int bollingerScore = bollingerBandsService.calculateBollingerWithSeries(symbol, period, nbDev, dateAtIndex, squeezeConfidence, priceType, series).getScore();

            double price = prices.getValue(index).doubleValue();

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            int bollingerScore2 = bollingerBandsService.calculateBollingerWithSeries(symbol, period, nbDev, dateAtIndex2, squeezeConfidence, priceType, series).getScore();

            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            if (bollingerScore == 1) {
                if (priceDiff > calculationConfidence) {
                    System.out.println("SUCCESS: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else if (bollingerScore == -1) {
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
            score = bollingerScore2;
        }

        System.out.println("---- END BOLLINGER BACKTEST ----\n");
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
        backtest.setIndicator("BOLLINGER");
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);

        backtestRepository.save(backtest);

        return backtest;
    }
    
    public Backtest dmiHistoricalBacktest(String symbol, int period, LocalDateTime date, String priceType, int backtestLookback, int lookbackPeriod, double calculationConfidence){
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

        System.out.println("---- START DMI BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            System.out.println("----");
            System.out.println("Index: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            int dmiScore = dmiService.calculateDmiWithSeries(symbol, period, dateAtIndex, series).getScore();

            double price = prices.getValue(index).doubleValue();

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            int dmiScore2 = dmiService.calculateDmiWithSeries(symbol, period, dateAtIndex2, series).getScore();

            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            if (dmiScore == 1) {
                if (priceDiff > calculationConfidence) {
                    System.out.println("SUCCESS: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else if (dmiScore == -1) {
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
            score = dmiScore2;
        }

        System.out.println("---- END DMI BACKTEST ----\n");
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
        backtest.setIndicator("DMI");
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);

        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest mfiHistoricalBacktest(String symbol, int period, LocalDateTime date, int lowerLimit, int upperLimit, String priceType, int backtestLookback, int lookbackPeriod, double calculationConfidence){
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

        System.out.println("---- START MFI BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            System.out.println("----");
            System.out.println("Index: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            int mfiScore = mfiService.calculateMfiWithSeries(symbol, period, dateAtIndex, lowerLimit, upperLimit, series).getScore();

            double price = prices.getValue(index).doubleValue();

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            int mfiScore2 = mfiService.calculateMfiWithSeries(symbol, period, dateAtIndex2, lowerLimit, upperLimit, series).getScore();

            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            if (mfiScore == 1) {
                if (priceDiff > calculationConfidence) {
                    System.out.println("SUCCESS: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else if (mfiScore == -1) {
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
            score = mfiScore2;
        }

        System.out.println("---- END MFI BACKTEST ----\n");
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
        backtest.setIndicator("MFI");
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);

        backtestRepository.save(backtest);

        return backtest;
    }

    public Backtest trendlineHistoricalBacktest(String symbol, int period, LocalDateTime date, int lookback, double slopeConfidence, String priceType, int backtestLookback, int lookbackPeriod, double calculationConfidence){
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

        System.out.println("---- START TRENDLINE BACKTEST ----");

        for (int i = backtestLookback; i > 0; i -= lookbackPeriod) {
            int index = targetIndex - i;
            if (index < 0) break;

            System.out.println("----");
            System.out.println("Index: " + index + "\n");

            LocalDateTime dateAtIndex = series.getBar(index).getEndTime().toLocalDateTime();
            int trendlineScore = trendlineService.calculateTrendlineWithSeries(symbol, period, dateAtIndex, lookback, series, slopeConfidence).getScore();

            double price = prices.getValue(index).doubleValue();

            int index2 = index + lookbackPeriod;
            if (index2 >= series.getBarCount()) break;

            LocalDateTime dateAtIndex2 = series.getBar(index2).getEndTime().toLocalDateTime();
            int trendlineScore2 = trendlineService.calculateTrendlineWithSeries(symbol, period, dateAtIndex2, lookback, series, slopeConfidence).getScore();

            double price2 = prices.getValue(index2).doubleValue();
            double priceDiff = (price2 - price) / price2;

            if (trendlineScore == 1) {
                if (priceDiff > calculationConfidence) {
                    System.out.println("SUCCESS: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
                    successCount++;
                } else
                    System.out.println("VIOLENCE: BUY predicted at " + dateAtIndex + " price moved " + priceDiff + " by " + dateAtIndex2);
            } else if (trendlineScore == -1) {
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
            score = trendlineScore2;
        }

        System.out.println("---- END TRENDLINE BACKTEST ----\n");
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
        backtest.setIndicator("TRENDLINE");
        backtest.setSignal(signal);
        backtest.setScore(score);
        backtest.setDate(targetDate);
        backtest.setConfidenceScore(confidenceWeight);

        backtestRepository.save(backtest);

        return backtest;
    }
*/
    // Helper Functions VIOLATE DRY (Don't Repeat Yourself) -> TODO: pack them within an utils dir

    private BarSeries loadSeries(String symbol) {

        if (repository.findBySymbol(symbol).isEmpty()) {
            throw new NotEnoughDataException("No data found for symbol: " + symbol);
        }

        List<StockDataDaily> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);
        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);

        for (StockDataDaily data : dataList) {
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

    private Indicator<Num> sourceSelector(String priceType, BarSeries series){
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
