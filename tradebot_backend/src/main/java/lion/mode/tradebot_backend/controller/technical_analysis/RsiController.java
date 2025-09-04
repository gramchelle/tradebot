package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.dto.indicators.RSIResult;
import lion.mode.tradebot_backend.service.technicalanalysis.RsiService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/rsi")
@RequiredArgsConstructor
public class RsiController {

    private final RsiService rsiService;

    @GetMapping("/{symbol}")
    public ResponseEntity<RSIResult> getRsiNow(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "2") int trendPeriod
    ) {
        return ResponseEntity.ok(rsiService.calculateRSI(symbol, period, trendPeriod));
    }

    @GetMapping("/{symbol}/at")
    public ResponseEntity<RSIResult> getRsiAt(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "2") int trendPeriod,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        return ResponseEntity.ok(rsiService.calculateRSI(symbol, period, date, trendPeriod));
    }
}