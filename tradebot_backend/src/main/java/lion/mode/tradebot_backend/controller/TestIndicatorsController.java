package lion.mode.tradebot_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.NEW_BaseIndicatorResponseDto;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.TestIndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
@Tag(name = "[!] FOR TESTING")
public class TestIndicatorsController {

    private final TestIndicatorService indicatorsService;

    @GetMapping("/rsi")
    public ResponseEntity<NEW_BaseIndicatorResponseDto> getRsi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant date,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(indicatorsService.calculateRsi(symbol, period, date, source), HttpStatus.OK);
    }

    @GetMapping("/bollinger-bands")
    public ResponseEntity<NEW_BaseIndicatorResponseDto>  getBollingerBands(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "SMA") String basisMAtype,
            @RequestParam(defaultValue = "2.0") double stdDev,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant date,
            @RequestParam(defaultValue = "close") String source
            ){
        return new ResponseEntity<>(indicatorsService.calculateBollingerBands(symbol, period, basisMAtype, stdDev, date, source), HttpStatus.OK);
    }

    @GetMapping("/macd")
    public ResponseEntity<NEW_BaseIndicatorResponseDto> getMacd(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant date,
            @RequestParam(defaultValue = "close") String source){
        return new ResponseEntity<>(indicatorsService.calculateMACD(symbol, shortPeriod, longPeriod, signalPeriod, date, source), HttpStatus.OK);
    }

    @GetMapping("/ma-crossover")
    public ResponseEntity<NEW_BaseIndicatorResponseDto> getMaCrossover(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "21") int longPeriod,
            @RequestParam(defaultValue = "SMA") String basisMAtype,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant date,
            @RequestParam(defaultValue = "close") String source){
        return new ResponseEntity<>(indicatorsService.calculateMaCrossover(symbol, shortPeriod, longPeriod, basisMAtype, date, source), HttpStatus.OK);
    }


    @GetMapping("/mfi")
    public ResponseEntity<NEW_BaseIndicatorResponseDto> getMfi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant date){
        return new ResponseEntity<>(indicatorsService.calculateMfi(symbol, period, date), HttpStatus.OK);
    }

    @GetMapping("/dmi")
    public ResponseEntity<NEW_BaseIndicatorResponseDto> getDmi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant date,
            @RequestParam(defaultValue = "close") String source){
        return new ResponseEntity<>(indicatorsService.calculateDmi(symbol, period, date, source), HttpStatus.OK);
    }

    @GetMapping("/trend")
    public ResponseEntity<NEW_BaseIndicatorResponseDto> getTrend(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant date){
        return new ResponseEntity<>(indicatorsService.calculateTrend(symbol, period, date), HttpStatus.OK);
    }

}
