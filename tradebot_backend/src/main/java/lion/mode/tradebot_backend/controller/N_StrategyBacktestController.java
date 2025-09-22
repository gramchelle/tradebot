package lion.mode.tradebot_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.base_responses.N_StrategyBacktestDto;
import lion.mode.tradebot_backend.service.N_technicalanalysis.N_StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/strategy")
@RequiredArgsConstructor
@Tag(name = "Backtest Strategy")
public class N_StrategyBacktestController {

    private final N_StrategyService strategyService;

    @GetMapping("/rsi")
    public ResponseEntity<N_StrategyBacktestDto> backtestRsi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        try{
            return new ResponseEntity<>(strategyService.runRsiStrategyBacktest(symbol, source, period, upperLimit, lowerLimit, lookback), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/macd")
    public ResponseEntity<N_StrategyBacktestDto> backtestMacd(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        try{
            return new ResponseEntity<>(strategyService.runMacdStrategyBacktest(symbol, source, shortPeriod, longPeriod, signalPeriod, lookback), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dmi")
    public ResponseEntity<N_StrategyBacktestDto> backtestDmi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "20") int adxThreshold,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        try{
            return new ResponseEntity<>(strategyService.runDmiStrategyBacktest(symbol, source, period, adxThreshold, lookback), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/mfi")
    public ResponseEntity<N_StrategyBacktestDto> backtestMfi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        try{
            return new ResponseEntity<>(strategyService.runMfiStrategyBacktest(symbol, source, period, upperLimit, lowerLimit, lookback), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ema-crossover")
    public ResponseEntity<N_StrategyBacktestDto> backtestEma(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "21") int longPeriod,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        try{
            return new ResponseEntity<>(strategyService.runEmaCrossoverStrategyBacktest(symbol, source, shortPeriod, longPeriod, lookback), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sma-crossover")
    public ResponseEntity<N_StrategyBacktestDto> backtestSma(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "21") int longPeriod,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        try{
            return new ResponseEntity<>(strategyService.runSmaCrossoverStrategyBacktest(symbol, source, shortPeriod, longPeriod, lookback), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/bollinger-bands")
    public ResponseEntity<N_StrategyBacktestDto> backtestBollingerBands(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2") int stdDevMultiplier,
            @RequestParam(defaultValue = "SMA") String basisMaType,
            @RequestParam(defaultValue = "close") String source,
            @RequestParam(defaultValue = "252") int lookback
    ){
        try{
            return new ResponseEntity<>(strategyService.runBollingerBandsStrategyBacktest(symbol, source, period, stdDevMultiplier, basisMaType, lookback), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
