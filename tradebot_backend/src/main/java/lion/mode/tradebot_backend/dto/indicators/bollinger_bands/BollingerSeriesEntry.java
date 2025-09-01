package lion.mode.tradebot_backend.dto.indicators.bollinger_bands;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BollingerSeriesEntry {

    private LocalDateTime timestamp;
    private double close;
    private double middle;
    private double upper;
    private double lower;
    private double bandwidth;

}