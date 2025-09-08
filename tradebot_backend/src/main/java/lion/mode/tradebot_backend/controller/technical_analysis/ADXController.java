package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.dto.indicators.ADXResult;
import lion.mode.tradebot_backend.service.technicalanalysis.ADXService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/adx")
@RequiredArgsConstructor
public class ADXController {

    private final ADXService adxService;

    @GetMapping("/{symbol}")
    public ResponseEntity<ADXResult> getADXAtDate(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "14") int adxPeriod,
            @RequestParam(defaultValue = "1") int lookback) {

        ADXResult result = adxService.calculateADXAt(symbol, adxPeriod, lookback, date);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}