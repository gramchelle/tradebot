package lion.mode.tradebot_backend.dto.indicator;

import lombok.Data;

@Data
public class MACrossResult {

    private String symbol;

    private int shortPeriod;

    private int longPeriod;

    private String maType;

    private double shortMAValue;

    private double longMAValue;

    private double crossoverPoint;

    private double confidence;

    private String signal;

    private int score;
}