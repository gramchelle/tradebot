package lion.mode.tradebot_backend.dto.technicalanalysis.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalkForwardRequestDto {

    /* Walk Forward Parameters */

    @Schema(description = "Stock symbol", example = "AAPL", required = true)
    private String symbol;

    @JsonIgnore
    private String interval = "1d";

    @Schema(description = "Optimization window in days", example = "252", required = true)
    private int optimizationWindow = 252;

    @Schema(description = "Validation window in days", example = "126", required = true)
    private int validationWindow = 126;

    @Schema(description = "Rolling step in days", example = "25", required = true)
    private int rollStep = 25;

    @JsonIgnore
    private int step = 1;

    @Schema(description = "List of technical indicators with their parameters")
    private List<IndicatorParam> indicators;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorParam {
        @Schema(description = "Indicator type", example = "rsi and trendline and bollinger and ema_cross and sma_cross and macd and mfi and dmi and ema10 and ema20 and ema30 and ema50 and ema100 and ema200 and sma10 and sma20 and sma30 and sma50 and sma100 and sma200", required = true)
        private String type;

        @Schema(description = "Parameters for the indicator", example = "{\"rsiPeriod\":14,\"bollingerPeriod\":20,\"bollingerStdDev\":2,\"takeProfit\":15.0,\"stopLoss\":3.0,\"trailingStopLoss\":1}", required = true)
        private Map<String, Object> params;
    }
}