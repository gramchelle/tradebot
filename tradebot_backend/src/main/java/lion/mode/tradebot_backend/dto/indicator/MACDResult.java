package lion.mode.tradebot_backend.dto.indicator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class MACDResult {
    
    private String symbol;

    @JsonIgnore
    private int shortPeriod;

    @JsonIgnore
    private int longPeriod;

    @JsonIgnore
    private int signalPeriod;

    private double macdScore;

    private double signalScore;

    private double histogramValue;

    @JsonIgnore
    private int histogramTrendPeriod;

    @JsonIgnore
    private String histogramTrend;

    @JsonIgnore
    private String divergence;

    @JsonIgnore
    private String maCross;

    private String signal;

    private int score;

}
