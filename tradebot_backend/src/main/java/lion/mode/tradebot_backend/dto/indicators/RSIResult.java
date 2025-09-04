package lion.mode.tradebot_backend.dto.indicators;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class RSIResult {

    private String symbol;

    private int period;

    @JsonIgnore
    private LocalDateTime date;

    private double rsiValue;

    private String signal;

    private int score;

    // private double confidence = 0; // Later in the process

    private String divergence = "none detected yet";

    @JsonIgnore
    private int trendPeriod = 2;

    private String trendComment;

}