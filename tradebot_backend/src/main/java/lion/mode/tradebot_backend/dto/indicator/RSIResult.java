package lion.mode.tradebot_backend.dto.indicator;

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

    @JsonIgnore //ignore in serialization
    private String divergence = "none detected yet";

    @JsonIgnore
    private int trendPeriod;

    @JsonIgnore
    private String trendComment;

}