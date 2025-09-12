package lion.mode.tradebot_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "backtest_result")
@Data
public class BacktestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double confidenceScore;
    private double avgReturn;
    private double winRate;
    private double avgWin;
    private double avgLoss;
    private double riskReward;
    private double sharpeRatio;
    private double maxDrawdown;
    private Integer tradeFrequency;

    @OneToOne
    @JoinColumn(name = "backtest_id")
    private BacktestEntity backtest;

}
