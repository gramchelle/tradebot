package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class RSIResult {

    String symbol;
    double rsiValue;
    String signal;
    
}
