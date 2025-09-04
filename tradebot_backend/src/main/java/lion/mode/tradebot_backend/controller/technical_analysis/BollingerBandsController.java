package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.dto.indicators.BollingerResult;
import lion.mode.tradebot_backend.service.technicalanalysis.BollingerService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/bollinger")
@RequiredArgsConstructor
public class BollingerBandsController {

    private final BollingerService bollingerService;

    @GetMapping("/{symbol}")
    public ResponseEntity<BollingerResult> getLatestBollinger(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2.0") double nbDev,
            @RequestParam(defaultValue = "1h") String interval) {

        BollingerResult result = bollingerService.calculateLatest(symbol, period, nbDev);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Belirli bir tarihteki Bollinger Bands sonucu
     */
    @GetMapping("/{symbol}/at")
    public ResponseEntity<BollingerResult> getBollingerAtDate(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDate,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2.0") double nbDev,
            @RequestParam(defaultValue = "1d") String interval) {

        BollingerResult result = bollingerService.calculateAtDate(symbol, period, nbDev, targetDate);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
