package lion.mode.tradebot_backend.dto.indicators.rsi;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RSIResult {

    private String symbol;
    private int period;
    private LocalDateTime date;
    private double rsiValue;
    private String signal;       // Buy, Sell, Hold
    private int score;
    private String divergence = "none detected yet";
    private String trendComment;

}