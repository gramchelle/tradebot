package lion.mode.tradebot_backend.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lion.mode.tradebot_backend.dto.statistical.CorrelationResult;
import lion.mode.tradebot_backend.dto.statistical.MeanReversionResult;
import lion.mode.tradebot_backend.service.StatisticalAnalysisService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stata")
public class StatisticalAnalysisController {

    private final StatisticalAnalysisService statisticalAnalysisService;

    @GetMapping("/mean-reversion/{symbol}")
    public ResponseEntity<MeanReversionResult> getMeanReversionSignal(@PathVariable String symbol, @RequestParam int period) {
        return new ResponseEntity<>(statisticalAnalysisService.calculateMeanReversionSignal(symbol, period), HttpStatus.OK);
    }
    
    @GetMapping("/correlation/{symbolA}/{symbolB}")
    public ResponseEntity<CorrelationResult> getCorrelation(@PathVariable String symbolA, @PathVariable String symbolB, @RequestParam int period) {
        return new ResponseEntity<>(statisticalAnalysisService.calculateCorrelation(symbolA, symbolB, period), HttpStatus.OK);
    }
}
