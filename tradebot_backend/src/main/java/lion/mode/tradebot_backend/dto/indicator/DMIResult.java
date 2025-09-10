package lion.mode.tradebot_backend.dto.indicator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class DMIResult {

    private String symbol;

    private int period;

    private double adxScore;

    @JsonIgnore
    private String adxTrend;

    private double plusDi;

    private double minusDi;

    private String signal;

    private int score;
}