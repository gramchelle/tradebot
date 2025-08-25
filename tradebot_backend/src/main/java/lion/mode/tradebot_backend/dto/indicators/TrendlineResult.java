package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrendlineResult {
    
    private String symbol;
    private String timeframe; // e.g. 1d 1h
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private double startPrice;
    private double endPrice;

    private String trendType;   // uptrend, downtrend, sideways
    private double slope;
    private double intercept;

    private String lineType;    // support, resistance, none
    private int touchCount;    
    
    private boolean isBroken;
    private LocalDateTime breakoutDate;
    private double breakoutPrice;

    private double confidence = 1.0 ;
    private String signal;      
    private String comment;
}
