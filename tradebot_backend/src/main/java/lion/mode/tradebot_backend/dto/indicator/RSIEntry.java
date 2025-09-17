package lion.mode.tradebot_backend.dto.indicator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RSIEntry {

    private String symbol;
    private LocalDateTime date;
    private int period = 14;
    private int upperLimit = 70;
    private int lowerLimit = 30;
    private String source;

}