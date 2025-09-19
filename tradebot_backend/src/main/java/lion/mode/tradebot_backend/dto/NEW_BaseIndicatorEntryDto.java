package lion.mode.tradebot_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class NEW_BaseIndicatorEntryDto {

    private String symbol;
    private String indicatorName;
    private Instant date;
    private int period;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> values = new HashMap<>();
}
