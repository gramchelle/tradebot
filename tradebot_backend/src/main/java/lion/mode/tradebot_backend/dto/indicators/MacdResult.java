package lion.mode.tradebot_backend.dto.indicators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class MacdResult { // MACD is a momentum indicator that shows the relationship between two moving averages of a security’s price.
    
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

    private String signal;

    private int score;

}
