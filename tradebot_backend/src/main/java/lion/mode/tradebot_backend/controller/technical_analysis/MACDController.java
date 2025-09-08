package lion.mode.tradebot_backend.controller.technical_analysis;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lion.mode.tradebot_backend.dto.indicators.MACDResult;
import lion.mode.tradebot_backend.service.technicalanalysis.MACDService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ta/macd")
@RequiredArgsConstructor
public class MACDController {

    private final MACDService macdService;

    @GetMapping("/{symbol}")
    public ResponseEntity<MACDResult> getMacdAt(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "5") int histogramTrendPeriod,
            @RequestParam(defaultValue = "0.00005") double histogramConfidence,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        return new ResponseEntity<>(
                macdService.calculateMacd(symbol, shortPeriod, longPeriod, signalPeriod, date, histogramTrendPeriod, histogramConfidence), HttpStatus.OK);
    }
}