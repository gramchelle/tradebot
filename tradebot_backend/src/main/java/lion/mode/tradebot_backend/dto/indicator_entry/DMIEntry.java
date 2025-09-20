package lion.mode.tradebot_backend.dto.indicator_entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DMIEntry {

    private String symbol;
    private int period;
    private Instant date;
    private double strongTrendThreshold = 25.0;
    private double moderateTrendThreshold = 20.0;
    private double significantDiDiff = 3.0;

}
