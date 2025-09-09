package lion.mode.tradebot_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "backtest_results")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Backtest {

    @Id
    @Column(name = "backtestId", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "indicator", nullable = false)
    private String indicator;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "strategy_id", nullable = false)
//    private Strategy strategy;

    @Column(name = "signal", nullable = false)
    private String signal;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "stockDate", nullable = false)
    private LocalDateTime stockDate;

    @Column(name = "confidenceScore")
    private double confidenceScore;

//    @Column(name = "isMatch", nullable = false)
//    private boolean isMatch;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
