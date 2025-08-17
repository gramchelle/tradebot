package lion.mode.tradebot_backend.dto.statistical;

import lombok.Data;

@Data
public class CorrelationResult { // correlationresult dto will be used for determining pairs trading strategies

    private String pair;
    private String symbolA;
    private String symbolB;
    private int period;
    private int commonDataPoints;
    private double correlation;
    
}
