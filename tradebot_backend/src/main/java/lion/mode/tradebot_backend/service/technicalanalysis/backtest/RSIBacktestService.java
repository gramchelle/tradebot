package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.dto.base_responses.BaseBacktestResponse;
import lion.mode.tradebot_backend.dto.base_responses.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.RSIEntry;
import lion.mode.tradebot_backend.dto.indicator.TrendlineEntry;
import lion.mode.tradebot_backend.model.Backtest;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.service.technicalanalysis.IndicatorService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.RSIService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.TrendlineService;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import java.time.Instant;
import java.util.*;

@Service
public class RSIBacktestService extends IndicatorService {

    private final RSIService service;
    private final TrendlineService trendlineService;

    public RSIBacktestService(StockDataRepository repository, BacktestRepository backtestRepository, RSIService service, TrendlineService trendlineService) {
        super(repository, backtestRepository);
        this.service = service;
        this.trendlineService = trendlineService;
    }

    public BaseBacktestResponse runBacktest(RSIEntry entry, int lookback, int horizon, String timeInterval, double takeProfit, double stopLoss, int tradeAmount) {
        String symbol = entry.getSymbol().toUpperCase();
        Instant date = entry.getDate();
        int period = entry.getPeriod();
        int upperLimit = entry.getUpperLimit();
        int lowerLimit = entry.getLowerLimit();
        String source = entry.getSource();

        BarSeries series = loadSeries(symbol);
        int targetIndex = seriesAmountValidator(symbol, series, date);

        double currentCapital = (double) tradeAmount;
        double initialCapital = currentCapital;
        double maxCapital = currentCapital;
        double maxDrawdown = 0.0;

        TrendlineEntry trendlineEntry = new TrendlineEntry(symbol, 14, date, lookback, 0.9, 3);
        double latestRsiValue = 0.0;
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
        String currentSignal = "HOLD";

        BaseBacktestResponse response = new BaseBacktestResponse();
        response.setSymbol(symbol);
        response.setIndicator("RSI");
        response.setTimeInterval(timeInterval);
        response.setStopLossPercentage(stopLoss);
        response.setTakeProfitPercentage(takeProfit);

        int truePositives = 0, trueNegatives = 0, falsePositives = 0, falseNegatives = 0;
        double signalThreshold = 0.02;

        int startIndex = Math.max(period + 1, targetIndex - lookback);

        for (int i = startIndex; i <= targetIndex; i++) {
            if (i >= series.getBarCount()) break;

            RSIEntry currentEntry = new RSIEntry();
            currentEntry.setSymbol(symbol);
            currentEntry.setDate(series.getBar(i).getEndTime());
            currentEntry.setPeriod(period);
            currentEntry.setUpperLimit(upperLimit);
            currentEntry.setLowerLimit(lowerLimit);
            currentEntry.setSource(source);

            BaseIndicatorResponse rsiResponse = service.calculateWithSeries(currentEntry, series);
            if (rsiResponse == null || rsiResponse.getSignal() == null) continue;

            if (rsiResponse.getValues() != null && rsiResponse.getValues().containsKey("rsiValue"))
                latestRsiValue = rsiResponse.getValues().get("rsiValue");

            if (rsiResponse.getBarsSinceSignal() != -1) barsSinceLastSignal = rsiResponse.getBarsSinceSignal();

            String signal = convertToSimpleSignal(rsiResponse.getSignal());
            lastSignal = signal;

            Num currentPriceNum = series.getBar(i).getClosePrice();
            Num futurePriceNum = i + horizon < series.getBarCount() ? series.getBar(i + horizon).getClosePrice() : currentPriceNum;

            // --- BAR BAZLI SINYAL DOĞRULUK HESABI ---
            String outcome = evaluateSignalOutcome(signal, currentPriceNum, futurePriceNum, signalThreshold);
            switch (outcome) {
                case "TP": truePositives++; break;
                case "TN": trueNegatives++; break;
                case "FP": falsePositives++; break;
                case "FN": falseNegatives++; break;
            }

            // --- POZİSYON AÇ/KAPA LİKİDİTE LOGİĞİ ---
            double currentPrice = currentPriceNum.doubleValue();
            if (!inPosition && signal.equalsIgnoreCase("Buy")) {
                inPosition = true;
                entryPrice = currentPrice;
                entryIndex = i;
                currentSignal = signal;

            } else if (inPosition && (signal.equalsIgnoreCase("Sell") || signal.equalsIgnoreCase("HOLD") || i == targetIndex)) {
                double exitPrice = currentPrice;
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
                tradeDurations.add(i - entryIndex);

                response.setBarsSinceLastTrade(barsSinceLastSignal);

                maxCapital = Math.max(maxCapital, currentCapital);
                double currentDrawdown = (maxCapital - currentCapital) / maxCapital;
                maxDrawdown = Math.max(maxDrawdown, currentDrawdown);

                inPosition = false;
                currentSignal = "HOLD";
            }
        }

        // --- KAPALI POZİSYON KONTROLÜ ---
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

        // --- METRİKLER ---
        double percentageReturn = ((currentCapital - initialCapital) / initialCapital) * 100;
        double winRate = totalTrades > 0 ? (double) winningTrades / totalTrades : 0.0;
        double avgWin = winningTrades > 0 ? totalProfit / winningTrades : 0.0;
        double avgLoss = losingTrades > 0 ? totalLoss / losingTrades : 0.0;
        double avgTradeDuration = tradeDurations.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        double volatility = calculateVolatility(returns);
        double sharpeRatio = calculateSharpeRatio(returns, 0.02);
        double sortinoRatio = calculateSortinoRatio(returns, 0.02);

        int totalSignalEvaluations = truePositives + trueNegatives + falsePositives + falseNegatives;
        double accuracy = totalSignalEvaluations > 0 ? (double) (truePositives + trueNegatives) / totalSignalEvaluations : 0.0;
        double precision = calculatePrecision(truePositives, falsePositives);
        double recall = calculateRecall(truePositives, falseNegatives);
        double f1Score = calculateF1Score(precision, recall);

        // --- RESPONSE ---
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
        parameters.put("period", period);
        parameters.put("upperLimit", upperLimit);
        parameters.put("lowerLimit", lowerLimit);
        parameters.put("rsiValue", latestRsiValue);
        response.setIndicatorParameters(parameters);

        Map<String, Double> detailedMetrics = new HashMap<>();
        detailedMetrics.put("tradeAmount", (double) tradeAmount);
        detailedMetrics.put("initialCapital", initialCapital);
        detailedMetrics.put("finalCapital", currentCapital);
        detailedMetrics.put("winningTrades", (double) winningTrades);
        detailedMetrics.put("losingTrades", (double) losingTrades);
        detailedMetrics.put("signalThreshold", signalThreshold);
        detailedMetrics.put("f1Score", f1Score);
        detailedMetrics.put("totalSignalEvaluations", (double) totalSignalEvaluations);
        response.setDetailedMetrics(detailedMetrics);

        return response;
    }


    public boolean saveIndicatorBacktest(RSIEntry entry, int lookback, int horizon, String timeInterval, double takeProfit, double stopLoss, int tradeAmount) {
        BaseBacktestResponse response = runBacktest(entry, lookback, horizon, timeInterval, takeProfit, stopLoss, tradeAmount);
        try{
            Backtest backtest = new Backtest();
            backtest.setSymbol(response.getSymbol());
            backtest.setIndicator("RSI");
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
