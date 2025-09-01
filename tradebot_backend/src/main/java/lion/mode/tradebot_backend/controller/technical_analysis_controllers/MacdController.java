package lion.mode.tradebot_backend.controller.technical_analysis_controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lion.mode.tradebot_backend.dto.indicators.macd.MacdResult;
import lion.mode.tradebot_backend.dto.indicators.macd.MacdResultDivergence;
import lion.mode.tradebot_backend.service.technicalanalysis.MacdService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ta/macd")
@RequiredArgsConstructor
public class MacdController {

    private final MacdService macdService;

    @GetMapping("/{symbol}")
    public ResponseEntity<MacdResult> getMacd(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "12") int shortP,
            @RequestParam(defaultValue = "26") int longP,
            @RequestParam(defaultValue = "9") int signalP
    ) {
        return ResponseEntity.ok(macdService.calculateMacd(symbol, shortP, longP, signalP));
    }

    @GetMapping("/{symbol}/at")
    public ResponseEntity<MacdResult> getMacdAt(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "12") int shortP,
            @RequestParam(defaultValue = "26") int longP,
            @RequestParam(defaultValue = "9") int signalP,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        return ResponseEntity.ok(
            macdService.calculateMacdAt(symbol, shortP, longP, signalP, dateTime)
        );
    }

    @GetMapping("/{symbol}/at/divergence")
    public ResponseEntity<MacdResultDivergence> getMacdAtWithDivergence(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "12") int shortP,
            @RequestParam(defaultValue = "26") int longP,
            @RequestParam(defaultValue = "9") int signalP,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam(defaultValue = "10") int lookback
    ) {
        return ResponseEntity.ok(
            macdService.calculateMacdAtWithDivergence(symbol, shortP, longP, signalP, dateTime, lookback)
        );
    }
}
