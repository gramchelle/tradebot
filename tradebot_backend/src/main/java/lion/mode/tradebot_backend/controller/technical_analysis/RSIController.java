package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.dto.indicators.RSIResult;
import lion.mode.tradebot_backend.service.technicalanalysis.RSIService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/rsi")
@RequiredArgsConstructor
public class RSIController {

    private final RSIService rsiService;

    @GetMapping("/{symbol}")
    public ResponseEntity<RSIResult> getRsiAt(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "1") int trendPeriod,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "70")  int upperLimit,
            @RequestParam(defaultValue = "20") int lookbackPeriod
    ) {
        return ResponseEntity.ok(rsiService.calculateRSI(symbol, period, date, trendPeriod, lowerLimit, upperLimit, lookbackPeriod));
    }
}