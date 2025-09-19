package lion.mode.tradebot_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.BaseIndicatorResponse;
import lion.mode.tradebot_backend.dto.indicator.*;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/technical-analysis")
@RequiredArgsConstructor
@Tag(name = "Technical Analysis Indicators")
public class IndicatorsController {

    private final RSIService rsiService;
    private final MACDService macdService;
    private final MACrossoverService macrossService;
    private final BollingerBandsService bollingerService;
    private final TrendlineService trendlineService;
    private final MFIService mfiService;
    private final DMIService dmiService;

    @GetMapping("/rsi")
    public ResponseEntity<BaseIndicatorResponse> calculateRSI(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "close") String source) {
        RSIEntry entry = new RSIEntry(symbol, date, period, upperLimit, lowerLimit, source);
        return new ResponseEntity<>(rsiService.calculate(entry), HttpStatus.OK);
    }

    @GetMapping("/macd")
    public ResponseEntity<BaseIndicatorResponse> calculateMACD(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "5") int histogramTrendPeriod,
            @RequestParam(defaultValue = "0.00005") double histogramConfidence,
            @RequestParam(defaultValue = "close") String source) {
        MACDEntry entry = new MACDEntry(symbol, date, shortPeriod, longPeriod, signalPeriod, histogramTrendPeriod, histogramConfidence, source);
        return new ResponseEntity<>(macdService.calculate(entry), HttpStatus.OK);
    }

    @GetMapping("/ma-crossover")
     public ResponseEntity<BaseIndicatorResponse> calculateEmaCrossover(
             @RequestParam String symbol,
             @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
             @RequestParam(defaultValue = "9") int shortPeriod,
             @RequestParam(defaultValue = "26") int longPeriod,
             @RequestParam(defaultValue = "EMA") String maType,
             @RequestParam(defaultValue = "5") int lookback,
             @RequestParam(defaultValue = "close") String source,
             @RequestParam(defaultValue = "0.05") double relativeThreshold){
         MACrossoverEntry entry = new MACrossoverEntry(symbol, date, lookback, shortPeriod, longPeriod, source, maType, relativeThreshold);
         return new ResponseEntity<>(macrossService.calculate(entry), HttpStatus.OK);
     }

    @GetMapping("/bollinger-bands")
    public ResponseEntity<BaseIndicatorResponse> calculateBollingerBands(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "2.0") double numberOfDeviations,
            @RequestParam(defaultValue = "5.0") double squeezeConfidence,
            @RequestParam(defaultValue = "close") String source){
        BollingerBandsEntry entry = new BollingerBandsEntry(symbol, period, date, numberOfDeviations, source, squeezeConfidence);
        return new ResponseEntity<>(bollingerService.calculate(entry), HttpStatus.OK);
    }

    @GetMapping("/trendline")
    public ResponseEntity<BaseIndicatorResponse> calculateTrendline(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "30") int lookback,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "0.05") double slopeConfidence,
            @RequestParam(defaultValue = "3") int supportResistanceTouchAmount){
        TrendlineEntry entry = new TrendlineEntry(symbol, period, date, lookback, slopeConfidence, supportResistanceTouchAmount);
        return new ResponseEntity<>(trendlineService.calculate(entry), HttpStatus.OK);
    }

    @GetMapping("/mfi")
    public ResponseEntity<BaseIndicatorResponse> calculateMFI(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "20") int lowerLimit,
            @RequestParam(defaultValue = "80") int upperLimit) {
        MFIEntry entry = new MFIEntry(symbol, date, period, upperLimit, lowerLimit);
        return new ResponseEntity<>(mfiService.calculate(entry), HttpStatus.OK);
    }

    @GetMapping("/dmi") // TODO: Add sensitivity confidence
    public ResponseEntity<BaseIndicatorResponse> calculateDMI(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "25") double strongTrendThreshold,
            @RequestParam(defaultValue = "20") double moderateTrendThreshold,
            @RequestParam(defaultValue = "3.0") double diDiff){
        DMIEntry entry = new DMIEntry(symbol, period, date, strongTrendThreshold, moderateTrendThreshold, diDiff);
        return new ResponseEntity<>(dmiService.calculate(entry), HttpStatus.OK);
    }
}
