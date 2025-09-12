package lion.mode.tradebot_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "backtest")
@Data
public class BacktestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String timeframe;
    private int lookback;
    private int horizon;

    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime endedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id")
    private Strategy strategy;

    @OneToOne(mappedBy = "backtest", cascade = CascadeType.ALL, orphanRemoval = true)
    private BacktestResult result;

}
