package lion.mode.tradebot_backend.controller.technical_analysis_controllers;

import lion.mode.tradebot_backend.dto.indicators.bollinger_bands.BollingerResult;
import lion.mode.tradebot_backend.dto.indicators.bollinger_bands.BollingerSeriesEntry;
import lion.mode.tradebot_backend.service.technicalanalysis.BollingerService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ta/bollinger")
@RequiredArgsConstructor
public class BollingerBandsController {

    private final BollingerService bollingerService;

    @GetMapping("/bollinger")
    public ResponseEntity<BollingerResult> getBollinger(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2.0") double nbDev
    ) {
        return ResponseEntity.ok(bollingerService.calculateBollinger(symbol, period, nbDev));
    }

    @GetMapping("/bollinger/range")
    public ResponseEntity<BollingerResult> getBollingerRange(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2.0") double nbDev,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        if (start.isAfter(end)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(bollingerService.calculateBollinger(symbol, period, nbDev, start, end));
    }

    @GetMapping("/bollinger/series")
    public ResponseEntity<List<BollingerSeriesEntry>> getBollingerSeries(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2.0") double nbDev,
            @RequestParam(defaultValue = "500") int lookback
    ) {
        return ResponseEntity.ok(bollingerService.getBollingerSeries(symbol, period, nbDev, lookback));
    }

    @GetMapping("/bollinger/remote")
    public ResponseEntity<?> getBollingerRemote(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "60min") String interval,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2.0") double nbdevUp,
            @RequestParam(defaultValue = "2.0") double nbdevDn
    ) {
        try {
            return ResponseEntity.ok(bollingerService.fetchBbandsFromAlphaVantage(symbol, interval, period, nbdevUp, nbdevDn));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

}
