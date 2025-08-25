package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class BollingerResult {

    private String symbol;
    private int period = 14;
    
    private double upperBand;
    private double middleBand;
    private double lowerBand;
    private double closePrice;

    private double stdDev;        
    private double bandWidth;     
    private double percentB;      

    private String volatilityTrend; 
    private String signal;          
    private double confidence = 1.0;
    private String comment = "none";
}