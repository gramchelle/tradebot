package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.dto.indicators.*;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.service.DataCollectorService;
import lion.mode.tradebot_backend.service.technicalanalysis.RsiService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ta")
@RequiredArgsConstructor
public class TechnicalAnalysisController {

    private final DataCollectorService dataCollectorService;
    private final RsiService rsiService;

    @GetMapping("/rsi")
    public ResponseEntity<RSIResult> getRSIScore(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period) {
        RSIResult rsiResult = rsiService.calculateRSI(symbol, period);
        return new ResponseEntity<>(rsiResult, HttpStatus.OK);
    }

    @GetMapping("/rsi/range")
    public ResponseEntity<RSIResult> getRSIForRange(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        RSIResult rsiResult = rsiService.calculateRSI(symbol, period, startDate, endDate);
        return ResponseEntity.ok(rsiResult);
    }

}