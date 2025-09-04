package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class MFIResult {

    private String symbol;

    private int period = 14;

    private double mfiValue;

    private String signal = "none"; // buy / sell / hold

    private int score;

}
