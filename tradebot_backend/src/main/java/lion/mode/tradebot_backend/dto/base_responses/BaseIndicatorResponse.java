package lion.mode.tradebot_backend.dto.base_responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class BaseIndicatorResponse {

    private String symbol;
    private String indicator;
    private Instant date;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;
    
    private String signal;
    private double score;
    private int barsSinceSignal = -1;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Double> values = new HashMap<>();

}
