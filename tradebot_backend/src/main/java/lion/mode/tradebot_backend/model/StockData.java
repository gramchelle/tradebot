package lion.mode.tradebot_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_data")
//@Table(name = "nasdaq_stock_data")
@Data
public class StockData { // db entity which saves ohlcv data

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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