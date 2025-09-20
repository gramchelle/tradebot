package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.*;
import java.util.List;

public abstract class AbstractBacktestService {

    protected final StockDataRepository repository;
    protected final BacktestRepository backtestRepository;

    protected AbstractBacktestService(StockDataRepository repository, BacktestRepository backtestRepository) {
        this.repository = repository;
        this.backtestRepository = backtestRepository;
    }

    protected BarSeries loadSeries(String symbol) {

        if (repository.findBySymbol(symbol).isEmpty()) {
            throw new NotEnoughDataException("No data found for symbol: " + symbol);
        }

        List<StockDataDaily> dataList = repository.findBySymbolOrderByTimestampAsc(symbol);
        BarSeries series = new BaseBarSeriesBuilder()
                .withName(symbol)
                .build();


        for (StockDataDaily data : dataList) {
            LocalDateTime localEndTime = data.getTimestamp();
            ZonedDateTime zonedEndTime = localEndTime.atZone(ZoneId.systemDefault());
            Instant endTimeInstant = zonedEndTime.toInstant();

            if (series.getBarCount() > 0) {
                Instant lastEnd = series.getBar(series.getEndIndex()).getEndTime();
                if (!endTimeInstant.isAfter(lastEnd)) {
                    continue;
                }
            }

            Bar bar = new BaseBar(
                    Duration.ofDays(1),
                    endTimeInstant,
                    DecimalNum.valueOf(data.getOpen()),
                    DecimalNum.valueOf(data.getHigh()),
                    DecimalNum.valueOf(data.getLow()),
                    DecimalNum.valueOf(data.getClose()),
                    DecimalNum.valueOf(data.getVolume()),
                    DecimalNum.valueOf(0),
                    0
            );

            series.addBar(bar);
        }

        return series;
    }

    protected int seriesAmountValidator(String symbol, BarSeries series, Instant date) {
        int targetIndex = -1;
        for (int i = 0; i < series.getBarCount(); i++) {
            Instant barEndTime = series.getBar(i).getEndTime();
            if (!barEndTime.isAfter(date)) {
                targetIndex = i;
            } else break;
        }

        if (targetIndex == -1) {
            throw new NotEnoughDataException("No bar found before or at " + date + " for " + symbol);
        }

        return targetIndex;
    }

    protected Indicator<Num> sourceSelector(String priceType, BarSeries series) {
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

    protected Double calculatePrecision(int truePositives, int falsePositives) { // hit rate
        if (truePositives + falsePositives == 0) return 0.0;
        return (double) truePositives / (truePositives + falsePositives);
    }

    protected Double calculateRecall(int truePositives, int falseNegatives) {
        if (truePositives + falseNegatives == 0) return 0.0;
        return (double) truePositives / (truePositives + falseNegatives);
    }

    protected Double calculateF1Score(Double precision, Double recall) {
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
                if (priceWentUp) return "TP";
                if (priceWentDown) return "FP";
                return "NEUTRAL";

            case "SELL":
                if (priceWentDown) return "TN";
                if (priceWentUp) return "FN";
                return "NEUTRAL";

            case "HOLD":
                return "NEUTRAL";

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

        return volatility > 0 ? (avgReturn - riskFreeRate / 252) / volatility : 0.0; // Daily risk-free rate
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
