package lion.mode.tradebot_backend.dto.technicalanalysis.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllSymbolsBacktestResult {

    private String symbol;
    private String strategyName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String signal;
    private double totalProfitLossRatioPercent;
    
    private String maResult;
    private String indicatorResult;

}
