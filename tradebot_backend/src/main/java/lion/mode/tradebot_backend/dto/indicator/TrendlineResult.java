package lion.mode.tradebot_backend.dto.indicator;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class TrendlineResult {

    private String symbol;

    private int period;

    // Lookback period for support - resistance calculation
    @JsonIgnore
    private int lookback;

    @JsonIgnore
    private LocalDateTime date;

    private double slope;

    private String direction;

    private String signal;

    private int score;

    @JsonIgnore
    private String comment;

    private double intercept;

    @JsonIgnore
    private int startIndex;

    private boolean actsAsSupport;

    private boolean actsAsResistance;
}
