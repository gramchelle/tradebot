package lion.mode.tradebot_backend.dto.technicalanalysis.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastDecisionResponse {

    private String symbol;
    private Instant date;
    private double score;
    private double confidence;
    private String lastSignal;

}
