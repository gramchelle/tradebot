package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class MACrossoverResult {
    
    String symbol;
    String signal;
    double shortEma;
    double longEma;
}
