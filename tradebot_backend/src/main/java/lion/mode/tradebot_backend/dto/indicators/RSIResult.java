package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class RSIResult {

    private String symbol;
    private int period;    // traditionally default for 14
    private double rsiValue;
    private String signal;      // "buy", "sell", "hold"
    private int score;          // -1, 0, +1
    private double confidence = 1.0;  // default for 1.0 now
    
}
