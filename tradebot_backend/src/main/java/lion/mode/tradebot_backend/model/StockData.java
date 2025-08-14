package lion.mode.tradebot_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_data")
@Data
public class StockData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    private LocalDateTime timestamp;

}