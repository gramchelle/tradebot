package lion.mode.tradebot_backend.dto.indicators.adx_dmi;

import lombok.Data;

@Data
public class ADXResult {

    private double adx;
    private double plusDi; // positive directional indicator, used to determine the strength of the trend
    private double prevAdx;
    private double prev2Adx; // previous and 2 previous adx values are stored to calculate the trend
    // the user should be able to select the number of previous adx values to consider for the trend calculation
    private int adxPeriod = 14;
    private int adxLookback = 3; // this is the number of previous adx values to consider
    private String signal = "none"; // "buy" / "sell" / "hold"
    private int score;

}
