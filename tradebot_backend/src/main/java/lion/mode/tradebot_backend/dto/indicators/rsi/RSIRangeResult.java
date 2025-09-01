package lion.mode.tradebot_backend.dto.indicators.rsi;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class RSIRangeResult {

    private String symbol;
    private int period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<RSIResult> bars;
    
}