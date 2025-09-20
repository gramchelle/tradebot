package lion.mode.tradebot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TESTStrategyBacktestDto {

    private String symbol;
    private String strategyName;

    // Performans / Kar
    private double totalProfit;
    private double grossReturn;
    private double averageProfit;
    private double returnOverMaxDrawdown;
    private double rewardRiskRatio;

    // Risk / Drawdown
    private double maximumDrawdown;
    private double averageDrawdown;
    private double ulcerIndex;

    // İşlem Bazlı
    private int numberOfTrades;
    private int numberOfPositions;
    private double averageHoldingPeriod;
    private double totalHoldingPeriod;

    // Başarı Oranları
    private double winningPositionsRatio;
    private double losingPositionsRatio;
    private double winningTradesRatio;

    // Özelleşmiş / Benchmark
    private double buyAndHoldReturn;
    private double versusBuyAndHold;
    private double cashFlow;
    private double profitLoss;
}
