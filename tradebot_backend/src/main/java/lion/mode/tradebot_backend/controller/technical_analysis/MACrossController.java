package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.dto.indicators.MACrossResult;
import lion.mode.tradebot_backend.service.technicalanalysis.MACrossService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ta/ma-cross")
@RequiredArgsConstructor
public class MACrossController {

    private final MACrossService macrossService;

    @GetMapping("/EMA/{symbol}")
    public ResponseEntity<MACrossResult> getEMACrossScore(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod) {

        MACrossResult macrossResult = macrossService.calculateEMACross(symbol, shortPeriod, longPeriod);
        return new ResponseEntity<>(macrossResult, HttpStatus.OK);
    }

    @GetMapping("/SMA/{symbol}")
    public ResponseEntity<MACrossResult> getSMACrossScore(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod) {

        MACrossResult macrossResult = macrossService.calculateSMACross(symbol, shortPeriod, longPeriod);
        return new ResponseEntity<>(macrossResult, HttpStatus.OK);
    }

    @GetMapping("/range/{symbol}")
    public ResponseEntity<MACrossResult> getMACrossInRange(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDate,
            @RequestParam(defaultValue = "30") int lookback,
            @RequestParam(defaultValue = "EMA") String maType) {

        MACrossResult result = macrossService.calculateMaCrossUntil(
                symbol, shortPeriod, longPeriod, targetDate, lookback, maType
        );
        return ResponseEntity.ok(result);
    }

}