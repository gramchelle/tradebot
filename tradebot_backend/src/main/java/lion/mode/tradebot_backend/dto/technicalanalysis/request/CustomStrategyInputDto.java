package lion.mode.tradebot_backend.dto.technicalanalysis.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto.IndicatorParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomStrategyInputDto {
    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("strategyName")
    private String strategyName;

    @JsonProperty("source")
    private String source = "close";

    @JsonProperty("stopLoss")
    private double stopLoss = 5.0;

    @JsonProperty("takeProfit")
    private double takeProfit = 15.0;

    @JsonProperty("isTrailingStopLoss")
    private int isTrailingStopLoss = 1;

    @JsonProperty("indicators")
    private List<IndicatorParam> indicators; 

    @JsonProperty("lookback")
    private int lookback = 500;
}

