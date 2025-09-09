package lion.mode.tradebot_backend.controller.technical_analysis.indicator;

import lion.mode.tradebot_backend.dto.indicator.MACrossResult;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.MACrossService;
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
    public ResponseEntity<MACrossResult> getEMACross(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "14") int lookback,
            @RequestParam(defaultValue = "close") String priceType) {

            MACrossResult result = macrossService.calculateEMACross(symbol, shortPeriod, longPeriod, date, lookback, priceType);
            return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/SMA/{symbol}")
    public ResponseEntity<MACrossResult> getSMACross(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "14") int lookback,
            @RequestParam(defaultValue = "close") String priceType) {

        MACrossResult result = macrossService.calculateSMACross(symbol, shortPeriod, longPeriod, date, lookback, priceType);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}