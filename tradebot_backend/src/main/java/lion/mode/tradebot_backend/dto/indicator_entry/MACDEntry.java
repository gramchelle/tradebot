package lion.mode.tradebot_backend.dto.indicator_entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MACDEntry {

    private String symbol;
    private Instant date;
    private int shortPeriod;
    private int longPeriod;
    private int signalPeriod;

    private int histogramTrendPeriod;
    private double histogramConfidence;
    private String source;
}
