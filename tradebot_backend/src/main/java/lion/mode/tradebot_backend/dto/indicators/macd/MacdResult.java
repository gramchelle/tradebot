package lion.mode.tradebot_backend.dto.indicators.macd;

import lombok.Data;

@Data
public class MacdResult { // MACD is a momentum indicator that shows the relationship between two moving averages of a security’s price.
    
    private String symbol;
    private int shortPeriod;
    private int longPeriod;
    private int signalPeriod;
    private double macd;
    private double signal;
    private double histogram;
    private String divergence;
    private String signalText;
    private int score;

}
