package lion.mode.tradebot_backend.dto.technicalanalysis.response;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovingAveragesOverallResponse {

    /* Moving averages and signals */
    
    private Map<String, String> movingAverages = new HashMap<>();
    private Map<String, Integer> signals = new HashMap<>();
    private String overallSignal;

}
