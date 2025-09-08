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
/* 
    @GetMapping("/{symbol}")
    public ResponseEntity<MACrossResult> getMACrossInRange(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "30") int lookback,
            @RequestParam(defaultValue = "EMA") String maType) {

        MACrossResult result = macrossService.calculateMaCrossUntil(
                symbol, shortPeriod, longPeriod, date, lookback, maType
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
*/
    @GetMapping("/{symbol}")
    public ResponseEntity<MACrossResult> getMACross(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "50") int shortPeriod,
            @RequestParam(defaultValue = "100") int longPeriod,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "15") int lookback
            ) {

        MACrossResult result = macrossService.calculateEMACrossUntil(symbol, shortPeriod, longPeriod, date ,lookback);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}