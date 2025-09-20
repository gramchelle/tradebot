package lion.mode.tradebot_backend.dto.base_responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class N_StrategyBacktestDto {

    private String symbol;
    private String strategyName;
    private int lookbackPeriod;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String currentSignal;
    private double score;
    private double lastDecisionValue;
    private String lastDecisionValueDescriptor;

    private double totalProfit;
    private double grossReturn;
    private double averageProfit;
    private double returnOverMaxDrawdown;
    private double rewardRiskRatio;

    private double maximumDrawdown;
    private double averageDrawdown;

    private int numberOfTrades;
    private int numberOfPositions;
    private double averageHoldingPeriod;
    private double totalHoldingPeriod;

    private double winningPositionsRatio;
    private double losingPositionsRatio;
    private double winningTradesRatio;

    private double buyAndHoldReturn;
    private double versusBuyAndHold;
    private double cashFlow;
    private double profitLoss;

    private double sharpeRatio;
    private double sortinoRatio;
}
