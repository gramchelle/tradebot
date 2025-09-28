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
public class WalkForwardRequestDtoNoSymbol {

    @Schema(description = "Time interval", example = "1d", defaultValue = "1d")
    private String interval = "1d";

    @Schema(description = "Optimization window size in trading days", example = "252", defaultValue = "252")
    private int optimizationWindow = 252;

    @Schema(description = "Validation window size in trading days", example = "126", defaultValue = "126")
    private int validationWindow = 126;

    @Schema(description = "Rolling step size in trading days", example = "25", defaultValue = "25")
    private int rollStep = 25;

    @JsonIgnore
    private int step = 1;

    @Schema(description = "List of technical indicators with their parameters")
    private List<IndicatorParam> indicators; // Kullanıcı seçtiği indikatörler

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorParam {
        private String type;
        private Map<String, Object> params;
    }
}
