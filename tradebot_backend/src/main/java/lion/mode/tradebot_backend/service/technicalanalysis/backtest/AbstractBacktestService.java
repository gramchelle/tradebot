package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import lion.mode.tradebot_backend.dto.BaseBacktestResponse;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;

abstract class AbstractBacktestService {

    protected final StockDataRepository repository;
    protected final BacktestRepository backtestRepository;

    protected AbstractBacktestService(StockDataRepository repository, BacktestRepository backtestRepository) {
        this.repository = repository;
        this.backtestRepository = backtestRepository;
    }

    // TODO: Make this method generic to support different timeframes
    protected BarSeries loadSeries(String symbol) {

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
                    .timePeriod(java.time.Duration.ofDays(1))
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

    protected int seriesAmountValidator(String symbol, BarSeries series, LocalDateTime date){
        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            LocalDateTime barTime = series.getBar(i).getEndTime().toLocalDateTime();
            if (!barTime.isAfter(date)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found before or at " + date + " for " + symbol);
        }
        
        return targetIndex;
    }

    protected Indicator<Num> sourceSelector(String priceType, BarSeries series){
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

    protected Long calculatePositionSize(Long tradingCapital, Num entryPrice, int lookbackPeriod){
        // Using 1% of trading capital for each trade as an example
        Num riskPerTrade = DecimalNum.valueOf(tradingCapital).multipliedBy(DecimalNum.valueOf(0.01));
        return riskPerTrade.dividedBy(entryPrice).longValue();
    }

    protected Long calculateStopLossPrice(Num entryPrice, int atrPeriod, double atrMultiplier){
        // Example: Using ATR for stop-loss calculation
        // This is a placeholder implementation; actual ATR calculation should be done using an ATR indicator
        Num atrValue = entryPrice.multipliedBy(DecimalNum.valueOf(0.02)); // Assuming ATR is 2% of entry price
        return entryPrice.minus(atrValue.multipliedBy(DecimalNum.valueOf(atrMultiplier))).longValue();
    }

    protected Long calculateTakeProfitPrice(Num entryPrice, double rewardRiskRatio, Long stopLossPrice){
        Num riskAmount = entryPrice.minus(DecimalNum.valueOf(stopLossPrice));
        return entryPrice.plus(riskAmount.multipliedBy(DecimalNum.valueOf(rewardRiskRatio))).longValue();
    }

    protected Double calculateAccuracy(int successfulTrades, int totalTrades){
        if (totalTrades == 0) return 0.0;
        return (double) successfulTrades / totalTrades;
    }

    protected Double calculatePrecision(int truePositives, int falsePositives){ // hit rate
        if (truePositives + falsePositives == 0) return 0.0;
        return (double) truePositives / (truePositives + falsePositives);
    }

    protected Double calculateRecall(int truePositives, int falseNegatives){
        if (truePositives + falseNegatives == 0) return 0.0;
        return (double) truePositives / (truePositives + falseNegatives);
    }

    protected Double calculateF1Score(Double precision, Double recall){
        if (precision + recall == 0) return 0.0;
        return 2 * (precision * recall) / (precision + recall);
    }

    protected String evaluateSignalOutcome(String signal, Num currentPrice, Num futurePrice, double threshold) {
        if (currentPrice == null || futurePrice == null) return "NEUTRAL";
        
        double priceChangePercent = futurePrice.minus(currentPrice).dividedBy(currentPrice).doubleValue();
        boolean priceWentUp = priceChangePercent > threshold;
        boolean priceWentDown = priceChangePercent < -threshold;
        
        switch (signal.toUpperCase()) {
            case "BUY":
                if (priceWentUp) return "TP";      // Predicted up, actually went up
                if (priceWentDown) return "FP";    // Predicted up, actually went down
                return "NEUTRAL";                   // Price didn't move significantly
                
            case "SELL":
                if (priceWentDown) return "TN";    // Predicted down, actually went down
                if (priceWentUp) return "FN";      // Predicted down, actually went up
                return "NEUTRAL";                   // Price didn't move significantly
                
            case "HOLD":
                return "NEUTRAL";                   // Hold signals are neutral
                
            default:
                return "NEUTRAL";
        }
    }

    protected String convertToSimpleSignal(String signal) {
        if (signal.toUpperCase() == null) return "HOLD";
        
        switch (signal.toUpperCase()) {
            case "STRONG BUY":
            case "BUY":
            case "WEAK BUY":
                return "Buy";
            case "STRONG SELL":
            case "SELL":
            case "WEAK SELL":
                return "Sell";
            default:
                return "Hold";
        }
    }
    
    protected double calculateVolatility(List<Double> returns) {
        if (returns.size() < 2) return 0.0;
        
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
            .mapToDouble(r -> Math.pow(r - mean, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }

    protected double calculateSharpeRatio(List<Double> returns, double riskFreeRate) {
        if (returns.isEmpty()) return 0.0;
        
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double volatility = calculateVolatility(returns);
        
        return volatility > 0 ? (avgReturn - riskFreeRate/252) / volatility : 0.0; // Daily risk-free rate
    }
    
    // Calculate Sortino ratio (downside deviation only)
    protected double calculateSortinoRatio(List<Double> returns, double riskFreeRate) {
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
