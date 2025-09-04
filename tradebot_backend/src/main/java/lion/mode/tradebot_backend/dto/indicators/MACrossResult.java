package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class MACrossResult {

    private String symbol;

    private int shortPeriod = 9;

    private int longPeriod = 21;

    private String maType;

    private double shortMAValue;

    private double longMAValue;

    private double crossoverPoint;

    private String signal;

    private int score;

    //private double confidence = 1.0;

    //private String trend = "none detected yet";
}