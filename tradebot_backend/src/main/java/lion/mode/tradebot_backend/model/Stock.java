// Bismillahirrahmanirrahim
package lion.mode.tradebot_backend.model;

import lombok.Data;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_data_historical")
@Data
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "symbol")
    private String symbol;

    @Column(name = "open")
    private double open;

    @Column(name = "high")
    private double high;

    @Column(name = "low")
    private double low;

    @Column(name = "close")
    private double close;

    @Column(name = "volume")
    private long volume;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

}