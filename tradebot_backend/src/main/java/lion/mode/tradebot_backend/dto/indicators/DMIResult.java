package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class DMIResult {

    private String symbol;

    private int period;

    private double adxScore;

    private String adxTrend;

    private double plusDi;

    private double minusDi;

    private String signal = "none determined yet";

    private int score; // -1, 0, 1
}