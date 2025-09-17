package lion.mode.tradebot_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseIndicatorResponse {

    private String symbol;
    private String indicator;
    private LocalDateTime date;
    private String status;
    private String signal;
    private int score;
    private double value;

}
