package lion.mode.tradebot_backend.dto.technicalanalysis.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastDecisionResponse {

    /* Decision matrix last decision response entity */
    
    private String symbol;
    private Instant date;
    private double score;
    private double confidence;
    private String lastSignal;

    @JsonIgnore
    private String strategyName;

    @JsonIgnore
    private String parameters;

}
