package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class MACDResult {
    
    private String symbol;

    private int shortPeriod;

    private int longPeriod;

    private int signalPeriod;

    private double macdScore;

    private double signalScore;

    private double histogramValue;

    private int histogramTrendPeriod;

    private String histogramTrend;

    private String divergence;

    private String maCross;

    private double confidence;

    private String signal;

    private int score;

    // TODO: Add seriesType and interval to calculate and send signal between that interval
    // seriesType The desired price type in the time series. Four types are supported: close, open, high, low

}
