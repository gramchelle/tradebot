package lion.mode.tradebot_backend.dto.indicators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ADXResult {

    private double adx;

    private double plusDi;

    private double prevAdx;

    private double prev2Adx; // previous and 2 previous adx values are stored to calculate the trend

    private int adxPeriod;

    @JsonIgnore
    private int adxLookback = 3; // TBD: this is the number of previous adx values to consider

    private String signal;

    private int score;

}
