package lion.mode.tradebot_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "strategies")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "strategy_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // ör: "RSI basic strategy"

    @Column(name = "indicator")
    private String indicator; // şimdilik tek indikatör

    // @Column(name = "params", columnDefinition = "TEXT")
    // private String params; // JSON string: {"period":14,"lower":30,"upper":70}

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

