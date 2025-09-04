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

    /**
     * Anlık ADX sonucu
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<ADXResult> getLatestADX(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int adxPeriod,
            @RequestParam(defaultValue = "3") int adxLookback) {

        ADXResult result = adxService.calculateADX(symbol, adxPeriod, adxLookback);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Belirli bir tarihteki ADX sonucu
     */
    @GetMapping("/{symbol}/at")
    public ResponseEntity<ADXResult> getADXAtDate(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDate,
            @RequestParam(defaultValue = "14") int adxPeriod,
            @RequestParam(defaultValue = "3") int adxLookback) {

        ADXResult result = adxService.calculateADXAt(symbol, adxPeriod, adxLookback, targetDate);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
// 19:49