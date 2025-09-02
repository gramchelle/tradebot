package lion.mode.tradebot_backend.controller.technical_analysis_controllers;

import lion.mode.tradebot_backend.dto.indicators.rsi.RSIResult;
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
            @RequestParam(defaultValue = "14") int period
    ) {
        return ResponseEntity.ok(rsiService.calculateRSI(symbol, period));
    }

    @GetMapping("/{symbol}/at")
    public ResponseEntity<RSIResult> getRsiAt(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        return ResponseEntity.ok(rsiService.calculateRSI(symbol, period, date));
    }

    @GetMapping("/{symbol}/divergence")
    public ResponseEntity<RSIResult> getRsiNowWithDivergence(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period
    ) {
        return ResponseEntity.ok(rsiService.calculateRSIWithDivergence(symbol, period));
    }

    @GetMapping("/{symbol}/at/divergence")
    public ResponseEntity<RSIResult> getRsiAtWithDivergence(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        return ResponseEntity.ok(rsiService.calculateRSIWithDivergence(symbol, period, date));
    }
}
