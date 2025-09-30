package lion.mode.tradebot_backend.dto.technicalanalysis.response;

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
public class BaseIndicatorResponseDto {

    /// Base response dto for Indicator results
    
    private String symbol;
    private String indicator;
    private Instant date;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Double> values = new HashMap<>();

}
