package lion.mode.tradebot_backend.controller.technical_analysis_controllers;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lion.mode.tradebot_backend.dto.indicators.rsi.RSIResult;
import lion.mode.tradebot_backend.service.technicalanalysis.RsiService;
import lombok.RequiredArgsConstructor;

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
        RSIResult result = rsiService.calculateRSI(symbol, period);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{symbol}/at")
    public ResponseEntity<RSIResult> getRsiAt(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        if (date == null) {
            date = LocalDateTime.now();
        }
        RSIResult result = rsiService.calculateRSI(symbol, period, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{symbol}/divergence")
    public ResponseEntity<RSIResult> getRsiNowWithDivergence(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period
    ) {
        RSIResult result = rsiService.calculateRSIWithDivergence(symbol, period);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{symbol}/at/divergence")
    public ResponseEntity<RSIResult> getRsiAtWithDivergence(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        if (date == null) {
            date = LocalDateTime.now();
        }
        RSIResult result = rsiService.calculateRSIWithDivergence(symbol, period, date);
        return ResponseEntity.ok(result);
    }
}
