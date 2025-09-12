package lion.mode.tradebot_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "strategy")
@Data
public class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;  // kullanıcı varsa

    private String name;

    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Strategy ↔ Indicator: Many-to-Many
    @ManyToMany
    @JoinTable(
        name = "strategy_indicator",
        joinColumns = @JoinColumn(name = "strategy_id"),
        inverseJoinColumns = @JoinColumn(name = "indicator_id")
    )
    private List<Indicator> indicators;

    // Strategy ↔ Backtest: One-to-Many
    @OneToMany(mappedBy = "strategy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BacktestEntity> backtests;
}
