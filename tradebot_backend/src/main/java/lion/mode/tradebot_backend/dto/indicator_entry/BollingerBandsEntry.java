package lion.mode.tradebot_backend.dto.indicator_entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BollingerBandsEntry {

    private String symbol;
    private int period;
    private Instant date;
    private double numberOfDeviations;
    private String source;
    private double squeezeConfidence;

}
