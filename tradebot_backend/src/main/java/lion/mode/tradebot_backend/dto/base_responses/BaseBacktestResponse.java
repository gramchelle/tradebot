package lion.mode.tradebot_backend.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
public class BaseBacktestResponse {

    private String symbol;
    private String indicator;
    private String signal;
    private double score;
    private String timeInterval = "1d";
    
    private int lookback;
    private int horizon;
    private String priceType;

    private double stopLossPercentage;
    private double takeProfitPercentage;

    // başarı metrikleri
    private double accuracy;
    private int totalTrades;
    private double winRate;
    private double totalProfit;
    private double percentageReturn;
    private double maxDrawdown;
    private double volatility;

    // trade bazlı metrikler
    private double avgWin;
    private double avgLoss;
    private double largestWin;
    private double largestLoss;
    private double averageTradeDuration;
    private double barsSinceLastTrade;
    private String supportOrResistance = "none";

    // Risk
    private double sharpeRatio;  // Risk ayarlı getiri
    private double sortinoRatio; // Negatif oynaklık ile risk ayarlı getiri

    private Map<String, Object> indicatorParameters;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Double> detailedMetrics;

    private Instant backtestStartDate;
    private Instant backtestEndDate;

}
