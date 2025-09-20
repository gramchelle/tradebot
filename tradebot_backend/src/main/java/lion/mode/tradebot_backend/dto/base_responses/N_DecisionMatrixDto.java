package lion.mode.tradebot_backend.dto.base_responses;

import lombok.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class N_DecisionMatrixDto {

    private String symbol;
    private Instant date;
    private Map<String, List<Object>> signal = new HashMap<>();
    private Map<String, Integer> signalCounts = new HashMap<>();
    private String overallDecision;

}
