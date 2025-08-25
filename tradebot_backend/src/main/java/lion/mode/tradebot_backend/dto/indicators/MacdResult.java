package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class MacdResult { // MACD is a momentum indicator that shows the relationship between two moving averages of a security’s price.
    
    private String symbol;
    private double shortPeriodEMA = 12;
    private double longPeriodEMA = 26;
    private double signalPeriod = 9;
    private double macdScore;
    private double signalScore;
    private double histogramValue;
    private double confidence = 1.0;
    private String tradeSignal;
    private String divergence;

}
