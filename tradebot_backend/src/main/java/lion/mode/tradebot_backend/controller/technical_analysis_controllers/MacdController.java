package lion.mode.tradebot_backend.controller.technical_analysis_controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lion.mode.tradebot_backend.dto.indicators.macd.MacdResult;
import lion.mode.tradebot_backend.dto.indicators.macd.MacdSeriesEntry;
import lion.mode.tradebot_backend.service.technicalanalysis.MacdService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ta/macd")
@RequiredArgsConstructor
public class MacdController {

    private final MacdService macdService;

    @GetMapping("/macd")
    public ResponseEntity<MacdResult> getMacd(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortP,
            @RequestParam(defaultValue = "26") int longP,
            @RequestParam(defaultValue = "9") int signalP
    ) {
        return ResponseEntity.ok(macdService.calculateMacd(symbol, shortP, longP, signalP));
    }

    @GetMapping("/macd/range")
    public ResponseEntity<MacdResult> getMacdRange(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortP,
            @RequestParam(defaultValue = "26") int longP,
            @RequestParam(defaultValue = "9") int signalP,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        if (start.isAfter(end)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(macdService.calculateMacd(symbol, shortP, longP, signalP, start, end));
    }

    @GetMapping("/macd/series")
    public ResponseEntity<List<MacdSeriesEntry>> getMacdSeries(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortP,
            @RequestParam(defaultValue = "26") int longP,
            @RequestParam(defaultValue = "9") int signalP,
            @RequestParam(defaultValue = "500") int lookback
    ) {
        return ResponseEntity.ok(macdService.getMacdSeries(symbol, shortP, longP, signalP, lookback));
    }
}
