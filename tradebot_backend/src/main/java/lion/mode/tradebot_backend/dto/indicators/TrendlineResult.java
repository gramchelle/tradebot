package lion.mode.tradebot_backend.dto.indicators;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TrendlineResult {

    private String symbol;

    private int period;

    private int lookback;

    private LocalDateTime date;

    private double slope;

    private String direction;

    private double confidence;

    private String signal;

    private int score;

    private String comment;

    private boolean actsAsSupport;

    private boolean actsAsResistance;
}
