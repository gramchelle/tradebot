package lion.mode.tradebot_backend.dto.indicators.macd;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MacdSeriesEntry {
    private LocalDateTime timestamp;
    private double macd;
    private double signal;
    private double histogram;
}