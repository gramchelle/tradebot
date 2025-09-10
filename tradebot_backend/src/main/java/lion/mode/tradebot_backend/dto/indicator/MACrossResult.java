package lion.mode.tradebot_backend.dto.indicator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class MACrossResult {

    private String symbol;
    
    @JsonIgnore
    private int shortPeriod;

    @JsonIgnore
    private int longPeriod;

    @JsonIgnore
    private String maType;

    private double shortMAValue;

    private double longMAValue;

    @JsonIgnore
    private double crossoverPoint;

    private String signal;

    private int score;
}