package lion.mode.tradebot_backend.dto.indicator;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class MACrossoverEntry {

    private String symbol;
    private LocalDateTime date;
    private int lookback;
    private int shortPeriod;
    private int longPeriod;
    private String source;
    private String maType;
    private double relativeThreshold;

}
