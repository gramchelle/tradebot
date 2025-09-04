package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class BollingerResult {

    private String symbol;

    private int period = 20;

    private double nbDev = 2.0;

    private double middle;

    private double upper;

    private double lower;

    private double bandwidth;

    private String squeeze = "none detected"; // "squeeze detected" / "squeeze not detected"

    private String signal = "none";

    private int score = 0; // 1 buy, -1 sell, 0 hold

}