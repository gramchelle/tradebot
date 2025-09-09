package lion.mode.tradebot_backend.dto.indicator;

import lombok.Data;

@Data
public class DMIResult {

    private String symbol;

    private int period;

    private double adxScore;

    private String adxTrend;

    private double plusDi;

    private double minusDi;

    private double confidence;

    private String signal;

    private int score;
}