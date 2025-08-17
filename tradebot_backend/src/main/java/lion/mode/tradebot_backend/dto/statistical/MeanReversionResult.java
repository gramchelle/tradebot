package lion.mode.tradebot_backend.dto.statistical;

import lombok.Data;

@Data
public class MeanReversionResult {

    String symbol;
    int period;
    double mean;
    double stdDev;
    double currentPrice;
    double zScore;
    double signalScore;

}
