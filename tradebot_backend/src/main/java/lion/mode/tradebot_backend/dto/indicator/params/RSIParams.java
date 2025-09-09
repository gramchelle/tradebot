package lion.mode.tradebot_backend.dto.indicator.params;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RSIParams {

    private Long id;
    private String symbol;
    private int period;
    private LocalDateTime date;
    private int lowerLimit;
    private int upperLimit;
    private String priceType;

}