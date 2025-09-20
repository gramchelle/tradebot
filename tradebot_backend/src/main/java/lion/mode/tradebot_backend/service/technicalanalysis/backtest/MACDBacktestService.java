package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lion.mode.tradebot_backend.service.technicalanalysis.IndicatorService;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import lion.mode.tradebot_backend.dto.base_responses.BaseBacktestResponse;
import lion.mode.tradebot_backend.dto.base_responses.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.MACDEntry;
import lion.mode.tradebot_backend.dto.indicator.TrendlineEntry;
import lion.mode.tradebot_backend.model.Backtest;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.MACDService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.TrendlineService;

@Service
public class MACDBacktestService extends IndicatorService {

    private final MACDService service;
    private final TrendlineService trendlineService;

    public MACDBacktestService(StockDataRepository repository, BacktestRepository backtestRepository, MACDService service, TrendlineService trendlineService) {
        super(repository, backtestRepository);
        this.service = service;
        this.trendlineService = trendlineService;
    }

    public BaseBacktestResponse runBacktest(MACDEntry entry, int lookback, int horizon, String timeInterval, double takeProfit, double stopLoss, int tradeAmount) {
        String symbol = entry.getSymbol().toUpperCase();
        Instant date = entry.getDate();
        int shortPeriod = entry.getShortPeriod();
        int longPeriod = entry.getLongPeriod();
        int signalPeriod = entry.getSignalPeriod();
        int histogramTrendPeriod = entry.getHistogramTrendPeriod();
        double histogramConfidence = entry.getHistogramConfidence();
        String source = entry.getSource();

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);

        // Init vars
        double currentCapital = (double) tradeAmount;
        double initialCapital = currentCapital;
        double maxCapital = currentCapital;
        double maxDrawdown = 0.0;

        TrendlineEntry trendlineEntry = new TrendlineEntry(symbol, 14, date, lookback, 0.9, 3);
        double latestRsiValue = 0.0; // Track the most recent RSI value
        String lastSignal = "";
        int barsSinceLastSignal = 0;
    
        List<Double> returns = new ArrayList<>();
        List<Double> trades = new ArrayList<>();
        List<Integer> tradeDurations = new ArrayList<>();
        
        int totalTrades = 0;
        int winningTrades = 0;
        int losingTrades = 0;
        
        double totalProfit = 0.0;
        double totalLoss = 0.0;
        double largestWin = 0.0;
        double largestLoss = 0.0;
        
        boolean inPosition = false;
        double entryPrice = 0.0;
        int entryIndex = 0;
        String currentSignal = "";

        // build response object
        BaseBacktestResponse response = new BaseBacktestResponse();
        response.setSymbol(symbol);
        response.setIndicator("MACD");
        response.setTimeInterval(timeInterval);
        response.setStopLossPercentage(stopLoss);
        response.setTakeProfitPercentage(takeProfit);
        
        int truePositives = 0, trueNegatives = 0, falsePositives = 0, falseNegatives = 0;
        double signalThreshold = 0.02; // price movement threshold

        int startIndex = Math.max(shortPeriod + 1, targetIndex - lookback);

        for (int i = startIndex; i <= targetIndex; i += horizon) {
            if (i >= series.getBarCount()) break;
            
            // Create MACDEntry entry for this specific date/index

            MACDEntry currentEntry = new MACDEntry();
            currentEntry.setSymbol(symbol);
            currentEntry.setDate(series.getBar(targetIndex).getEndTime());
            currentEntry.setShortPeriod(shortPeriod);
            currentEntry.setLongPeriod(longPeriod);
            currentEntry.setSignalPeriod(signalPeriod);
            currentEntry.setHistogramTrendPeriod(histogramTrendPeriod);
            currentEntry.setHistogramConfidence(histogramConfidence);
            currentEntry.setSource(source);
            
            BaseIndicatorResponse macdResponse = service.calculateWithSeries(currentEntry, series);
            if (macdResponse == null || macdResponse.getSignal() == null) continue;
                        
            // Update bars since last signal
            if (macdResponse.getBarsSinceSignal() != -1) barsSinceLastSignal = macdResponse.getBarsSinceSignal();
            
            if (!currentSignal.equalsIgnoreCase("Hold") && i + horizon < series.getBarCount()) {
                Num currentPrice = series.getBar(i).getClosePrice();
                Num futurePrice = series.getBar(i + horizon).getClosePrice();
                
                double priceChangePercent = futurePrice.minus(currentPrice).dividedBy(currentPrice).doubleValue();
                boolean priceWentUp = priceChangePercent > signalThreshold;
                boolean priceWentDown = priceChangePercent < -signalThreshold;
                
                if (currentSignal.equalsIgnoreCase("Buy")) {
                    if (priceWentUp) truePositives++;           // Predicted UP, actually went UP
                    else if (priceWentDown) falsePositives++;   // Predicted UP, actually went DOWN
                } else if (currentSignal.equalsIgnoreCase("Sell")) {
                    if (priceWentDown) trueNegatives++;         // Predicted DOWN, actually went DOWN
                    else if (priceWentUp) falseNegatives++;     // Predicted DOWN, actually went UP
                }
            }

            String signal = convertToSimpleSignal(macdResponse.getSignal());
            lastSignal = signal;
            double currentPrice = series.getBar(i).getClosePrice().doubleValue();
            
            // Execute trading logic
            if (!inPosition && (signal.equalsIgnoreCase("BUY") || signal.equalsIgnoreCase("STRONG BUY"))) {
                // Enter long position
                inPosition = true;
                entryPrice = currentPrice;
                entryIndex = i;
                currentSignal = signal;

            } else if (inPosition && (signal.equalsIgnoreCase("SELL") || signal.equalsIgnoreCase("STRONG SELL") || signal.equalsIgnoreCase("HOLD") || i == targetIndex)) {
                // Exit position
                double exitPrice = currentPrice;
                double tradeReturn = (exitPrice - entryPrice) / entryPrice;
                double tradeProfit = currentCapital * tradeReturn;
                
                // Update capital and tracking
                currentCapital += tradeProfit;
                totalTrades++;
                
                if (tradeProfit > 0) {
                    winningTrades++;
                    totalProfit += tradeProfit;
                    largestWin = Math.max(largestWin, tradeProfit);
                } else {
                    losingTrades++;
                    totalLoss += Math.abs(tradeProfit);
                    largestLoss = Math.min(largestLoss, tradeProfit);
                }
                
                trades.add(tradeProfit);
                returns.add(tradeReturn);
                tradeDurations.add(i - entryIndex);

                response.setBarsSinceLastTrade(barsSinceLastSignal);

                // Update max capital and drawdown
                maxCapital = Math.max(maxCapital, currentCapital);
                double currentDrawdown = (maxCapital - currentCapital) / maxCapital;
                maxDrawdown = Math.max(maxDrawdown, currentDrawdown);
                
                inPosition = false;
                currentSignal = "HOLD";
            }
        }
        
        // Force close any remaining position
        if (inPosition && targetIndex < series.getBarCount()) {
            double exitPrice = series.getBar(targetIndex).getClosePrice().doubleValue();
            double tradeReturn = (exitPrice - entryPrice) / entryPrice;
            double tradeProfit = currentCapital * tradeReturn;
            
            currentCapital += tradeProfit;
            totalTrades++;
            
            if (tradeProfit > 0) {
                winningTrades++;
                totalProfit += tradeProfit;
                largestWin = Math.max(largestWin, tradeProfit);
            } else {
                losingTrades++;
                totalLoss += Math.abs(tradeProfit);
                largestLoss = Math.min(largestLoss, tradeProfit);
            }
            
            trades.add(tradeProfit);
            returns.add(tradeReturn);
            tradeDurations.add(targetIndex - entryIndex);
        }
        
        // Calculate final metrics
        double percentageReturn = ((currentCapital - initialCapital) / initialCapital) * 100;
        double winRate = totalTrades > 0 ? (double) winningTrades / totalTrades : 0.0;
        double avgWin = winningTrades > 0 ? totalProfit / winningTrades : 0.0;
        double avgLoss = losingTrades > 0 ? totalLoss / losingTrades : 0.0;
        double avgTradeDuration = tradeDurations.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        
        // Calculate volatility and Sharpe ratio
        double volatility = calculateVolatility(returns);
        double sharpeRatio = calculateSharpeRatio(returns, 0.02); // Assuming 2% risk-free rate
        double sortinoRatio = calculateSortinoRatio(returns, 0.02);
        
        int totalSignalEvaluations = truePositives + trueNegatives + falsePositives + falseNegatives;
        double accuracy = totalSignalEvaluations > 0 ? (double)(truePositives + trueNegatives) / totalSignalEvaluations : 0.0;
        double precision = calculatePrecision(truePositives, falsePositives);
        double recall = calculateRecall(truePositives, falseNegatives);
        double f1Score = calculateF1Score(precision, recall);
        
        // Build response
        response.setSignal(lastSignal);
        response.setScore(totalTrades > 0 ? percentageReturn / 100 : 0.0);
        response.setAccuracy(accuracy);
        response.setTotalTrades(totalTrades);
        response.setWinRate(winRate);
        response.setTotalProfit(currentCapital - initialCapital);
        response.setPercentageReturn(percentageReturn);
        response.setMaxDrawdown(maxDrawdown);
        response.setVolatility(volatility);
        response.setSupportOrResistance(trendlineService.calculateWithSeries(trendlineEntry, series).getStatus());
        response.setSharpeRatio(sharpeRatio);
        response.setSortinoRatio(sortinoRatio);
        response.setAvgWin(avgWin);
        response.setAvgLoss(avgLoss);
        response.setLargestWin(largestWin);
        response.setLargestLoss(largestLoss);
        response.setAverageTradeDuration(avgTradeDuration);
        response.setLookback(lookback);
        response.setHorizon(horizon);
        response.setPriceType(source);
        response.setBacktestStartDate(series.getBar(startIndex).getEndTime());
        response.setBacktestEndDate(series.getBar(targetIndex).getEndTime());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("shortPeriod", shortPeriod);
        parameters.put("longPeriod", longPeriod);
        parameters.put("signalPeriod", signalPeriod);
        parameters.put("histogramTrendPeriod", histogramTrendPeriod);
        parameters.put("histogramConfidence", histogramConfidence);
        response.setIndicatorParameters(parameters);

        Map<String, Double> detailedMetrics = new HashMap<>();
        detailedMetrics.put("tradeAmount", (double) tradeAmount);
        detailedMetrics.put("initialCapital", initialCapital);
        detailedMetrics.put("finalCapital", currentCapital);
        detailedMetrics.put("winningTrades", (double) winningTrades);
        detailedMetrics.put("losingTrades", (double) losingTrades);
        detailedMetrics.put("signalThreshold", (double) signalThreshold);
        detailedMetrics.put("f1Score", f1Score);
        detailedMetrics.put("totalSignalEvaluations", (double) totalSignalEvaluations);
        response.setDetailedMetrics(detailedMetrics);

        return response;
    }

    public boolean saveIndicatorBacktest(MACDEntry entry, int lookback, int horizon, String timeInterval, double takeProfit, double stopLoss, int tradeAmount) {
        BaseBacktestResponse response = runBacktest(entry, lookback, horizon, timeInterval, takeProfit, stopLoss, tradeAmount);
        try{
            Backtest backtest = new Backtest();
            backtest.setSymbol(response.getSymbol());
            backtest.setIndicator("MACD");
            backtest.setSignal(response.getSignal());
            backtest.setScore(response.getScore());
            backtest.setTimeInterval(response.getTimeInterval());
            backtest.setStopLossPercentage(response.getStopLossPercentage());
            backtest.setTakeProfitPercentage(response.getTakeProfitPercentage());
            backtest.setAccuracy(response.getAccuracy());
            backtest.setTotalTrades(response.getTotalTrades());
            backtest.setWinRate(response.getWinRate());
            backtest.setTotalProfit(response.getTotalProfit());
            backtest.setPercentageReturn(response.getPercentageReturn());
            backtest.setMaxDrawdown(response.getMaxDrawdown());
            backtest.setVolatility(response.getVolatility());
            backtest.setAvgWin(response.getAvgWin());
            backtest.setAvgLoss(response.getAvgLoss());
            backtest.setLargestWin(response.getLargestWin());
            backtest.setLargestLoss(response.getLargestLoss());
            backtest.setAverageTradeDuration(response.getAverageTradeDuration());
            backtest.setBarsSinceLastTrade(response.getBarsSinceLastTrade());
            backtest.setSupportOrResistance(response.getSupportOrResistance());
            backtest.setSharpeRatio(response.getSharpeRatio());
            backtest.setSortinoRatio(response.getSortinoRatio());
            backtest.setBacktestStartDate(response.getBacktestStartDate());
            backtest.setBacktestEndDate(response.getBacktestEndDate());
            backtest.setIndicatorParameters(response.getIndicatorParameters());
            backtestRepository.save(backtest);
        } catch (Exception e) {
            System.out.println("[!] An error occurred while saving backtest parameters: " + e.getMessage());
        }
        return true;
    }
    
}
