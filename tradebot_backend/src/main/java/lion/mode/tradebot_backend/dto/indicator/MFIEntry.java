package lion.mode.tradebot_backend.dto.indicator;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MFIEntry {

    private String symbol;
    private Instant date;
    private int period = 14;
    private int upperLimit = 80;
    private int lowerLimit = 20;

}
