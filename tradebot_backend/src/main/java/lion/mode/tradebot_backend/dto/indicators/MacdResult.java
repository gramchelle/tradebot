package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class MacdResult { // MACD is a momentum indicator that shows the relationship between two moving averages of a security’s price.
    
    private String symbol;
    private double macd;
    private double signal;
    private double histogram;
    private String tradeSignal;

}
