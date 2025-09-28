package lion.mode.tradebot_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "walkforward_backtest_reports")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class WalkForwardReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    private String strategyName;
    private String symbol;
    private Instant runDate = Instant.now();
    private Instant startDate;
    private Instant endDate;

    private int optimizationWindow;
    private int validationWindow;
    private int stepSize;

    private double totalProfit;
    private double totalLoss;

    private double totalProfitLossRatio;
    private double totalProfitLossRatioPercent;

    private double grossReturn;
    private double averageProfit;
    private double returnOverMaxDrawdown;

    private int numberOfTrades;
    private int numberOfPositions;

    private String parameters;

    private String lastSignal;
    private Instant lastSignalDate;
    private double confidence;
    private double lastPrice;

}
