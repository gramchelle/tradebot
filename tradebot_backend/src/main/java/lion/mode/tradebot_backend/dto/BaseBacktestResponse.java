
package lion.mode.tradebot_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class BaseBacktestResponse {
    // Basic identification
    private String symbol;
    private String indicator;
    private String signal;           // Final signal recommendation
    private int score;              // Final score
    private LocalDateTime date;     // Target date for the signal

    // Performance metrics (from your backtest logic)
    private double accuracy;                    // Your "confidenceWeight" - successCount/trials
    private int totalTrials;                   // Total number of backtest trials
    private int successfulPredictions;         // Number of correct predictions
    private int failedPredictions;             // Number of incorrect predictions

    // Backtest configuration parameters
    private int lookbackPeriods;               // Total lookback period used
    private int lookbackPeriodStep;            // Step size between tests (your lookbackPeriod)
    private double calculationConfidence;      // Confidence threshold used
    private String priceType;                  // Price type used ("close", "open", etc.)

    // Indicator-specific parameters (for RSI)
    private Integer period;                    // RSI period (14, 21, etc.)
    private Integer upperLimit;                // Overbought threshold (70, 80, etc.)
    private Integer lowerLimit;                // Oversold threshold (30, 20, etc.)

    // Performance breakdown by signal type
    private SignalPerformance buySignalPerformance;
    private SignalPerformance sellSignalPerformance;
    private SignalPerformance holdSignalPerformance;

    // Statistical analysis
    private double averagePriceMovement;       // Average price movement observed
    private double maxPriceMovement;           // Maximum price movement
    private double minPriceMovement;           // Minimum price movement
    private double volatility;                 // Price movement volatility

    // Time analysis
    private LocalDateTime backtestStartDate;   // First date tested
    private LocalDateTime backtestEndDate;     // Last date tested
    private LocalDateTime calculatedAt;        // When backtest was performed

    // Additional context
    private String status;                     // "SUCCESS", "PARTIAL", "ERROR"
    private List<String> warnings;             // Any warnings during backtest
    private Map<String, Object> additionalMetrics; // Flexible field for custom metrics

    // Constructor
    public BaseBacktestResponse() {
        this.calculatedAt = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    // Helper methods
    public double getSuccessRate() {
        return totalTrials > 0 ? (double) successfulPredictions / totalTrials : 0.0;
    }

    public double getFailureRate() {
        return totalTrials > 0 ? (double) failedPredictions / totalTrials : 0.0;
    }

    public boolean isReliable() {
        return accuracy > 0.6 && totalTrials >= 10; // Configurable thresholds
    }

    public String getConfidenceLevel() {
        if (accuracy >= 0.8) return "High";
        if (accuracy >= 0.6) return "Medium";
        return "Low";
    }
}
