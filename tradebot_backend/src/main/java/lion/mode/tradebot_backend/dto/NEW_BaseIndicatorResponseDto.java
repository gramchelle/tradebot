package lion.mode.tradebot_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NEW_BaseIndicatorResponseDto {

    private String symbol;
    private String indicator;
    private Instant date;

//    private String signal;
//    private double score;
//    private int barsSinceSignal = -1;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Double> values = new HashMap<>();

}
