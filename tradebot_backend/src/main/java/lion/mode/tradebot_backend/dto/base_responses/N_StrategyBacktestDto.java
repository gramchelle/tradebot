package lion.mode.tradebot_backend.dto.base_responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class N_StrategyBacktestDto {

    // Summary report of backtest results for a strategy on a specific symbol
    private String symbol;
    private String strategyName;
    private int lookbackPeriod;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String parametersJson; 

    // karar
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String currentSignal;
    private double score;
    private double lastDecisionValue; 
    private String lastDecisionValueDescriptor; // confluence stratejilerde çalışır

    // performans
    private double totalProfit;
    private double totalLoss;

    private double totalProfitLossRatio;
    private double totalProfitLossRatioPercent;

    private double grossReturn;
    private double averageProfit;
    private double breakEvenCount; // break even trades: trades with no profit no loss

    private double profitCount;
    private double lossCount;

    // risk ve risk ayarlamaları
    private double returnOverMaxDrawdown;
    private double rewardRiskRatio;
    private double maximumDrawdown;
    private double averageDrawdown;

    // trade ve pozisyonlar
    private int numberOfTrades;
    private int numberOfPositions;
    private double averageHoldingPeriod;
    private double totalHoldingPeriod;

    // başarı oranları
    private double winningPositionsRatio;
    private double losingPositionsRatio;
    private double winningTradesRatio;

    // diğer metrikler
    private double buyAndHoldReturn;
    private double versusBuyAndHold;
    private double cashFlow;
    private double profitLoss;

    // risk ayarlanmış metrikler
    private double sharpeRatio;
    private double sortinoRatio;
}