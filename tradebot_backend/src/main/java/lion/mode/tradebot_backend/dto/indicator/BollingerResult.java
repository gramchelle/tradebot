package lion.mode.tradebot_backend.dto.indicator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class BollingerResult {

    private String symbol;

    private int period;

    @JsonIgnore
    private double numberOfDeviations = 2.0;

    private double upper;

    private double middle;

    private double lower;

    private double bandwidth;

    private String squeeze; // "detected" / "not detected"

    private String signal;

    private int score;

}