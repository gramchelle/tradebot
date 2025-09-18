package lion.mode.tradebot_backend.dto.indicator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

import jakarta.persistence.criteria.CriteriaBuilder.In;

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
