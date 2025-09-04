package lion.mode.tradebot_backend.dto.indicators;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TrendlineResult {

    private String symbol;

    private int period;

    private int lookback;

    private LocalDateTime date;

    private double slope;           // Trend eğimi

    private String direction;       // Uptrend, Downtrend, Sideways

    private String signal;          // Buy, Sell, Hold

    private int score;              // +1, -1, 0

    private String comment;         // Trend hakkında yorum

    private boolean actsAsSupport;  // true/false

    private boolean actsAsResistance; // true/false
}
