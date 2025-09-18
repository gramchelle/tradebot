package lion.mode.tradebot_backend.dto.indicator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

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
