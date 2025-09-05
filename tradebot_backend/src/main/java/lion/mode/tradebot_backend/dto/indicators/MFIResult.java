package lion.mode.tradebot_backend.dto.indicators;

import lombok.Data;

@Data
public class MFIResult {

    private String symbol;

    private int period;

    private double mfiValue;

    private double confidence;

    private String signal;

    private int score;

}
