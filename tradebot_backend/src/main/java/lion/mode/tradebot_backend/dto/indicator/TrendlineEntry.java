package lion.mode.tradebot_backend.dto.indicator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendlineEntry{

    private String symbol;
    private int period;
    private LocalDateTime date;
    private int lookback;
    private double slopeConfidence;

}
