package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class TrendlineResult {

    String symbol;
    double slope;
    double lastClose;
    double pastClose;
    String trend; // can be enumerated

}
