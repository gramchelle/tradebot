package lion.mode.tradebot_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lion.mode.tradebot_backend.utils.BacktestParameterHelper;
import lombok.Data;
import java.time.LocalDateTime;

import java.util.Map;

@Entity
@Table(name = "backtests")
@Data
public class Backtest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String indicator;
    private String signal;
    private double score;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "confidence_score")
    private double confidenceScore;

    private int totalTrials;
    private int successfulPredictions;
    private int failedPredictions;

    private int lookback;
    private int lookbackPeriod;
    private double calculationConfidence;
    private String priceType;

    @Column(name = "indicator_parameters", columnDefinition = "TEXT")
    @JsonIgnore
    private String indicatorParametersJson;

    private double averagePriceMovement;
    private double maxPriceMovement;
    private double minPriceMovement;
    private double volatility;

    private LocalDateTime backtestStartDate;
    private LocalDateTime backtestEndDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String status;

    @Transient
    private Map<String, Object> indicatorParameters;

    public Map<String, Object> getIndicatorParameters() {
        if (indicatorParameters == null && indicatorParametersJson != null) {
            indicatorParameters = BacktestParameterHelper.parametersFromJson(indicatorParametersJson);
        }
        return indicatorParameters;
    }

    public void setIndicatorParameters(Map<String, Object> parameters) {
        this.indicatorParameters = parameters;
        this.indicatorParametersJson = BacktestParameterHelper.parametersToJson(parameters);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
