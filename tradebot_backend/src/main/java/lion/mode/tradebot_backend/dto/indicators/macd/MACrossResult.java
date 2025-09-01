package lion.mode.tradebot_backend.dto.indicators.macd;

import lombok.Data;

@Data
public class MACrossResult {

    private String symbol;
    private int shortPeriod = 9;
    private int longPeriod = 21;
    private String maType = "EMA";
    private double shortMAValue;
    private double longMAValue;
    private double crossoverPoint;
    private String signal;
    private double confidence = 1.0;
    private String trend = "sideways";
}