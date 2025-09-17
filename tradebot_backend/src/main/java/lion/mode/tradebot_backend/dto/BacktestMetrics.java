package lion.mode.tradebot_backend.dto;

import lombok.Data;

@Data
public class BacktestMetrics {

    private double totalProfit;
    private double percentageReturn;
    private double annualizedReturn;
    private double cagr;

    // Risk
    private double maxDrawdown;
    private double maxDrawdownDuration;
    private double volatility;
    private double sharpeRatio;
    private double sortinoRatio;

    // Trade bazlı
    private int totalTrades;
    private double winRate;
    private double profitFactor;
    private double avgWin;
    private double avgLoss;
    private double medianWin;
    private double medianLoss;
    private double averageTradeDuration; // bar veya gün
    private double largestWin;
    private double largestLoss;

    // Ek analiz
    private double signalAgingDistribution; //taze sinyallerin başarısı (barsSinceSignal histogram)
    private double perMarketConditionPerformance; //yükseliş/pazar dönemlerinde performans (ör. ADX>25, bullish market)

}
