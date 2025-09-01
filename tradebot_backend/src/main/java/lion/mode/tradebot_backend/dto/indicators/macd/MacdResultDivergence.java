package lion.mode.tradebot_backend.dto.indicators.macd;

import lombok.Data;

@Data
public class MacdResultDivergence {
    private String symbol;
    private int shortPeriod;
    private int longPeriod;
    private int signalPeriod;
    private double macdScore;
    private double signalScore;
    private double histogramValue;
    private String divergence;
    private String signalText;
    private int score;

}
