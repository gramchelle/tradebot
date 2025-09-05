package lion.mode.tradebot_backend.dto.indicators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ADXResult {

    private double adx;

    private double plusDi;

    private double prevAdx;

    private int adxPeriod;

    @JsonIgnore
    private int adxLookback;

    private String trend;

}
