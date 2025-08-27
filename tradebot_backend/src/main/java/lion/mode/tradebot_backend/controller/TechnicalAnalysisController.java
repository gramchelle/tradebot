package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.dto.indicators.*;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.service.DataCollectorService;
import lion.mode.tradebot_backend.service.technicalanalysis.BollingerBandService;
import lion.mode.tradebot_backend.service.technicalanalysis.MACrossService;
import lion.mode.tradebot_backend.service.technicalanalysis.MacdService;
import lion.mode.tradebot_backend.service.technicalanalysis.RsiService;
import lion.mode.tradebot_backend.service.technicalanalysis.TrendlineService;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ta")
@RequiredArgsConstructor
public class TechnicalAnalysisController {

    private final RsiService rsiService;
    private final MacdService macdService;
    private final MACrossService macrossService;
    private final BollingerBandService bollingerBandService;
    private final TrendlineService trendlineService;

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

    @GetMapping("/macd")
    public ResponseEntity<MacdResult> getMacdScore(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod) {

        MacdResult macdResult = macdService.calculateMacd(symbol, shortPeriod, longPeriod, signalPeriod);
        return new ResponseEntity<>(macdResult, HttpStatus.OK);
    }

    @GetMapping("/macd/range")
    public ResponseEntity<MacdResult> getMacdScoreByDate(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        MacdResult macdResult = macdService.calculateMacd(symbol, shortPeriod, longPeriod, signalPeriod, startDate, endDate);
        return new ResponseEntity<>(macdResult, HttpStatus.OK);
    }

    @GetMapping("/ma-cross")
    public ResponseEntity<MACrossResult> getMACrossScore(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod) {

        MACrossResult macrossResult = macrossService.calculateMACross(symbol, shortPeriod, longPeriod);
        return new ResponseEntity<>(macrossResult, HttpStatus.OK);
    }

    @GetMapping("/bollinger")
    public ResponseEntity<BollingerResult> getBollingerBands(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period) {

        BollingerResult bollingerResult = bollingerBandService.calculateBollingerBands(symbol, period);
        return new ResponseEntity<>(bollingerResult, HttpStatus.OK);
    }

    @GetMapping("/bollinger/range")
    public ResponseEntity<List<BollingerResult>> getBollingerBandsForRange(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        List<BollingerResult> bollingerResults = bollingerBandService.calculateBollingerBandsForRange(symbol, period, startDate, endDate);
        return new ResponseEntity<>(bollingerResults, HttpStatus.OK);
    }

    @GetMapping("/trendline")
    public ResponseEntity<TrendlineResult> getTrendline(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int lookbackBars) {

        TrendlineResult trendlineResult = trendlineService.calculateTrendline(symbol, lookbackBars);
        return new ResponseEntity<>(trendlineResult, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllTechnicalIndicators(
            @RequestParam String symbol) {

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("rsi", rsiService.calculateRSI(symbol, 14).getSignal());
        results.put("macd", macdService.calculateMacd(symbol, 12, 26, 9).getTradeSignal());
        results.put("maCross", macrossService.calculateMACross(symbol, 9, 26).getSignal());
        results.put("bollingerMiddle", bollingerBandService.calculateBollingerBands(symbol, 20).getSignal());
        results.put("trendSlope", trendlineService.calculateTrendline(symbol, 14).getTrendType());

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

}