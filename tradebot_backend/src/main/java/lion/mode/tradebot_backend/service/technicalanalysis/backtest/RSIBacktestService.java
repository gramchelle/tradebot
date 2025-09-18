package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.dto.BaseBacktestResponse;
import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.RSIEntry;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.RSIService;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RSIBacktestService extends AbstractBacktestService {

    private final RSIService service;

    public RSIBacktestService(StockDataRepository repository, RSIService service) {
        super(repository);
        this.service = service;
    }

    public BaseBacktestResponse runRsiBacktest(RSIEntry entry, int lookback, int horizon, String timeInterval, double takeProfit, double stopLoss, int tradeAmount) {
        String symbol = entry.getSymbol().toUpperCase();
        LocalDateTime date = entry.getDate();
        int period = entry.getPeriod();
        int upperLimit = entry.getUpperLimit();
        int lowerLimit = entry.getLowerLimit();
        String source = entry.getSource();

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);

        // Trading simulation variables
        double currentCapital = (double) tradeAmount;
        double initialCapital = currentCapital;
        double maxCapital = currentCapital;
        double maxDrawdown = 0.0;
        
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
        
        // Position tracking
        boolean inPosition = false;
        double entryPrice = 0.0;
        int entryIndex = 0;
        String currentSignal = "";
        
        // TP/TN tracking
        int truePositives = 0, trueNegatives = 0, falsePositives = 0, falseNegatives = 0;
        double signalThreshold = 0.02; // 2% price movement threshold
        
        // Calculate start index ensuring we have enough lookback data
        int startIndex = Math.max(period + 1, targetIndex - lookback);
        
        // Main backtesting loop
        for (int i = startIndex; i <= targetIndex; i += horizon) {
            if (i >= series.getBarCount()) break;
            
            // Create RSI entry for this specific date/index
            RSIEntry currentEntry = new RSIEntry();
            currentEntry.setSymbol(symbol);
            currentEntry.setDate(series.getBar(i).getEndTime().toLocalDateTime());
            currentEntry.setPeriod(period);
            currentEntry.setUpperLimit(upperLimit);
            currentEntry.setLowerLimit(lowerLimit);
            currentEntry.setSource(source);
            
            // Get RSI signal using your existing service
            BaseIndicatorResponse rsiResponse = service.calculateWithSeries(currentEntry, series);
            if (rsiResponse == null || rsiResponse.getSignal() == null) continue;
            
            // Convert RSI signal to simple format
            String tradeBotSignal = "";
            if (rsiResponse.getSignal().equalsIgnoreCase("buy") || 
                rsiResponse.getSignal().equalsIgnoreCase("strong buy") || 
                rsiResponse.getSignal().equalsIgnoreCase("weak buy")) {
                tradeBotSignal = "BUY";
            } else if (rsiResponse.getSignal().equalsIgnoreCase("sell") || 
                       rsiResponse.getSignal().equalsIgnoreCase("strong sell") || 
                       rsiResponse.getSignal().equalsIgnoreCase("weak sell")) {
                tradeBotSignal = "SELL";
            } else {
                tradeBotSignal = "HOLD";
            }
            
            // Calculate accuracy by looking at future price movement
            if (!tradeBotSignal.equals("HOLD") && i + horizon < series.getBarCount()) {
                Num currentPrice = series.getBar(i).getClosePrice();
                Num futurePrice = series.getBar(i + horizon).getClosePrice();
                
                double priceChangePercent = futurePrice.minus(currentPrice).dividedBy(currentPrice).doubleValue();
                boolean priceWentUp = priceChangePercent > signalThreshold;
                boolean priceWentDown = priceChangePercent < -signalThreshold;
                
                // Evaluate signal accuracy
                if (tradeBotSignal.equals("BUY")) {
                    if (priceWentUp) truePositives++;           // Predicted UP, actually went UP
                    else if (priceWentDown) falsePositives++;   // Predicted UP, actually went DOWN
                } else if (tradeBotSignal.equals("SELL")) {
                    if (priceWentDown) trueNegatives++;         // Predicted DOWN, actually went DOWN
                    else if (priceWentUp) falseNegatives++;     // Predicted DOWN, actually went UP
                }
            }

            String signal = convertToSimpleSignal(rsiResponse.getSignal());
            double currentPrice = series.getBar(i).getClosePrice().doubleValue();
            
            // Execute trading logic
            if (!inPosition && (signal.equals("BUY") || signal.equals("STRONG_BUY"))) {
                // Enter long position
                inPosition = true;
                entryPrice = currentPrice;
                entryIndex = i;
                currentSignal = signal;
                
            } else if (inPosition && (signal.equals("SELL") || signal.equals("STRONG_SELL") || 
                                     signal.equals("HOLD") || i == targetIndex)) {
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
                
                // Update max capital and drawdown
                maxCapital = Math.max(maxCapital, currentCapital);
                double currentDrawdown = (maxCapital - currentCapital) / maxCapital;
                maxDrawdown = Math.max(maxDrawdown, currentDrawdown);
                
                // Reset position
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
        BaseBacktestResponse response = new BaseBacktestResponse();
        response.setSymbol(symbol);
        response.setIndicator("RSI");
        response.setDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());
        response.setSignal(currentSignal);
        response.setScore(totalTrades > 0 ? percentageReturn / 100 : 0.0);
        response.setTimeInterval(timeInterval);
        response.setAccuracy(accuracy);
        response.setTotalTrades(totalTrades);
        response.setWinRate(winRate);
        response.setTotalProfit(currentCapital - initialCapital);
        response.setPercentageReturn(percentageReturn);
        response.setMaxDrawdown(maxDrawdown);
        response.setVolatility(volatility);
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
        response.setBacktestStartDate(series.getBar(startIndex).getEndTime().toLocalDateTime());
        response.setBacktestEndDate(series.getBar(targetIndex).getEndTime().toLocalDateTime());
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("period", period);
        parameters.put("upperLimit", upperLimit);
        parameters.put("lowerLimit", lowerLimit);
        parameters.put("truePositives", truePositives);
        parameters.put("trueNegatives", trueNegatives);
        parameters.put("falsePositives", falsePositives);
        parameters.put("falseNegatives", falseNegatives);
        parameters.put("precision", precision);
        parameters.put("recall", recall);
        parameters.put("f1Score", f1Score);
        parameters.put("signalAccuracy", accuracy);
        parameters.put("totalSignalEvaluations", totalSignalEvaluations);
        response.setIndicatorParameters(parameters);

        Map<String, Double> detailedMetrics = new HashMap<>();
        detailedMetrics.put("tradeAmount", (double) tradeAmount);
        detailedMetrics.put("initialCapital", initialCapital);
        detailedMetrics.put("finalCapital", currentCapital);
        detailedMetrics.put("winningTrades", (double) winningTrades);
        detailedMetrics.put("losingTrades", (double) losingTrades);
        detailedMetrics.put("signalThreshold", (double) signalThreshold);
        response.setDetailedMetrics(detailedMetrics);

        return response;
    }
    
    private String convertToSimpleSignal(String rsiSignal) {
        if (rsiSignal == null) return "HOLD";
        
        switch (rsiSignal.toUpperCase()) {
            case "STRONG BUY":
            case "BUY":
            case "WEAK BUY":
                return "BUY";
            case "STRONG SELL":
            case "SELL":
            case "WEAK SELL":
                return "SELL";
            default:
                return "HOLD";
        }
    }
    
    private double calculateVolatility(List<Double> returns) {
        if (returns.size() < 2) return 0.0;
        
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
            .mapToDouble(r -> Math.pow(r - mean, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private double calculateSharpeRatio(List<Double> returns, double riskFreeRate) {
        if (returns.isEmpty()) return 0.0;
        
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double volatility = calculateVolatility(returns);
        
        return volatility > 0 ? (avgReturn - riskFreeRate/252) / volatility : 0.0; // Daily risk-free rate
    }
    
    // Calculate Sortino ratio (downside deviation only)
    private double calculateSortinoRatio(List<Double> returns, double riskFreeRate) {
        if (returns.isEmpty()) return 0.0;
        
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double dailyRiskFree = riskFreeRate / 252;
        
        double downsideVariance = returns.stream()
            .mapToDouble(r -> r < dailyRiskFree ? Math.pow(r - dailyRiskFree, 2) : 0.0)
            .average().orElse(0.0);
        
        double downsideDeviation = Math.sqrt(downsideVariance);
        
        return downsideDeviation > 0 ? (avgReturn - dailyRiskFree) / downsideDeviation : 0.0;
    }
}