package lion.mode.tradebot_backend.dto.indicator;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MACrossoverEntry {

    private String symbol;
    private Instant date;
    private int lookback;
    private int shortPeriod;
    private int longPeriod;
    private String source;
    private String maType;
    private double relativeThreshold;

}
