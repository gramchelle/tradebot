package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class MacdResult {
    
    private String symbol;
    private double macd;
    private double signal;
    private double histogram;
    private String tradeSignal;

}
