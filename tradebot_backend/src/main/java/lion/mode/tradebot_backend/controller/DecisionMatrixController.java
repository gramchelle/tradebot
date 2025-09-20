package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.dto.base_responses.N_DecisionMatrixDto;
import lion.mode.tradebot_backend.service.N_technicalanalysis.DecisionMatrixService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decision-matrix")
@RequiredArgsConstructor
public class DecisionMatrixController {

    private final DecisionMatrixService decisionMatrixService;

    @GetMapping("/{symbol}")
    public N_DecisionMatrixDto getDecisionMatrix(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "close") String source,
            @RequestParam(defaultValue = "252") int lookback) {

        return decisionMatrixService.getDecisionMatrix(symbol, source, lookback);
    }
}
