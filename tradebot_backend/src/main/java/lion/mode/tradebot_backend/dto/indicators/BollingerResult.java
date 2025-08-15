package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class BollingerResult {

    String symbol;
    double upperBand;
    double middleBand;
    double lowerBand;
    double closePrice;
    String signal;
}
