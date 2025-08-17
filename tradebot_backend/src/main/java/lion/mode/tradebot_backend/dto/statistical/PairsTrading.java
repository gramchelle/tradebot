package lion.mode.tradebot_backend.dto.statistical;

import lombok.Data;

@Data
public class PairsTrading {

    private String pair;
    private double meanSpread;
    private double stdDevSpread;
    private double currentSpread;
    private double zScore;
    private String signal;
    private String action;

}