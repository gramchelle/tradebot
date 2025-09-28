package lion.mode.tradebot_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "backtest_report")
@Data
public class Backtest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String strategyName;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String parametersJson; // indicator name eklenecek

    private String lastSignal;
    private double score;
    private String timeInterval = "1d";
    private int lookbackPeriod;

    private int totalTrades;
    private double totalProfit;
    private double totalLoss;
    private double totalProfitLossRatioPercent;
    
    private double returnOverMaxDrawdown;
    private double rewardRiskRatio;

    private double sharpeRatio;
    private double sortinoRatio;

    @Column(name = "backtest_start_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant backtestStartDate;

    @Column(name = "backtest_end_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant backtestEndDate = Instant.now();

}
