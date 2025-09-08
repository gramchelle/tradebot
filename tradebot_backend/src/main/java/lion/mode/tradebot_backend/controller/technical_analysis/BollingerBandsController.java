package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.dto.indicators.BollingerResult;
import lion.mode.tradebot_backend.service.technicalanalysis.BollingerBandsService;
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

    private final BollingerBandsService bollingerService;

    @GetMapping("/{symbol}")
    public ResponseEntity<BollingerResult> getBollingerAtDate(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2.0") double numberOfDeviations,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "0.05") double squeezeConfidence
    ) {
        BollingerResult result = bollingerService.calculateAtDate(symbol, period, numberOfDeviations, date, squeezeConfidence);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
