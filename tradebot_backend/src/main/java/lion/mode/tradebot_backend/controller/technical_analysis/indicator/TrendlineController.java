package lion.mode.tradebot_backend.controller.technical_analysis.indicator;

import lion.mode.tradebot_backend.dto.indicator.TrendlineResult;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.TrendlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/trendline")
@RequiredArgsConstructor
public class TrendlineController {

    private final TrendlineService trendlineService;

    @GetMapping("/{symbol}")
    public ResponseEntity<TrendlineResult> getTrendlineAt(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "30") int lookback,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "0.5") double slopeConfidence
    ) {
        return ResponseEntity.ok(trendlineService.calculateTrendline(symbol, period, lookback, date, slopeConfidence));
    }
}
