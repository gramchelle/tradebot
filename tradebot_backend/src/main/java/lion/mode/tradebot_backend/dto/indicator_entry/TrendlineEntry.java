package lion.mode.tradebot_backend.dto.indicator_entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendlineEntry{

    private String symbol;
    private int period;
    private Instant date;
    private int lookback;
    private double slopeConfidence;
    private int supportResistanceTouchAmount;

}
