package lion.mode.tradebot_backend.dto.indicator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ADXResult {

    private double adx;

    private int period;

    @JsonIgnore
    private double plusDi;

    @JsonIgnore
    private double prevAdx;

    @JsonIgnore
    private int lookback;

    @JsonIgnore
    private String trend;

}
