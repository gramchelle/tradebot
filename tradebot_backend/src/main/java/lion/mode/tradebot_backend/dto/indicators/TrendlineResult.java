package lion.mode.tradebot_backend.dto.indicators;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class TrendlineResult {

    private String symbol;

    private int period;

    // Lookback period for support - resistance calculation
    private int lookback;

    @JsonIgnore
    private LocalDateTime date;

    private double slope;

    private String direction;

    private double confidence = 0;

    private String signal;

    private int score;

    private String comment;

    private boolean actsAsSupport;

    private boolean actsAsResistance;
}
