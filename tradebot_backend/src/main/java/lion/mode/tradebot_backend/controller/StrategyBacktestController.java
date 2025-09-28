package lion.mode.tradebot_backend.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.technicalanalysis.request.CustomStrategyInputDto;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.AllSymbolsBacktestResult;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.StrategyBacktestDto;
import lion.mode.tradebot_backend.service.technicalanalysis.StrategyService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class StrategyBacktestController {

    private final StrategyService strategyService;

    @GetMapping("/rsi")
    public ResponseEntity<StrategyBacktestDto> backtestRsi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "0") int isTrailingLoss,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runRsiStrategyBacktest(symbol, source, stopLoss, takeProfit, period, upperLimit, lowerLimit, isTrailingLoss, lookback), HttpStatus.OK);
    }

    @GetMapping("/macd")
    public ResponseEntity<StrategyBacktestDto> backtestMacd(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "0") int isTrailingStopLoss,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runMacdStrategyBacktest(symbol, source, stopLoss, takeProfit, shortPeriod, longPeriod, signalPeriod, isTrailingStopLoss, lookback), HttpStatus.OK);
    }

    @GetMapping("/dmi")
    public ResponseEntity<StrategyBacktestDto> backtestDmi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "20") int adxThreshold,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "0") int isTrailingStopLoss,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runDmiStrategyBacktest(symbol, source, stopLoss, takeProfit, period, adxThreshold, isTrailingStopLoss, lookback), HttpStatus.OK);
    }

    @GetMapping("/mfi")
    public ResponseEntity<StrategyBacktestDto> backtestMfi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "0") int isTrailingStopLoss,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runMfiStrategyBacktest(symbol, source, stopLoss, takeProfit, period, upperLimit, lowerLimit, isTrailingStopLoss, lookback), HttpStatus.OK);
    }

    @GetMapping("/ema-crossover")
    public ResponseEntity<StrategyBacktestDto> backtestEma(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "21") int longPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "0") int isTrailingStopLoss,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runEmaCrossoverStrategyBacktest(symbol, source, stopLoss, takeProfit, shortPeriod, longPeriod, isTrailingStopLoss, lookback), HttpStatus.OK);
    }

    @GetMapping("/sma-crossover")
    public ResponseEntity<StrategyBacktestDto> backtestSma(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "21") int longPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "0") int isTrailingStopLoss,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runSmaCrossoverStrategyBacktest(symbol, source, stopLoss, takeProfit, shortPeriod, longPeriod, isTrailingStopLoss, lookback), HttpStatus.OK);
    }

    @GetMapping("/bollinger-bands")
    public ResponseEntity<StrategyBacktestDto> backtestBollingerBands(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2") int stdDevMultiplier,
            @RequestParam(defaultValue = "SMA") String basisMaType,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "0") int isTrailingStopLoss,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runBollingerBandsStrategyBacktest(symbol, source, stopLoss, takeProfit, period, stdDevMultiplier, basisMaType, isTrailingStopLoss, lookback), HttpStatus.OK);

    }
/*
    @PostMapping("/custom-strategy")
    public ResponseEntity<?> backtestCustomStrategy(@RequestBody CustomStrategyInputDto inputDto){
        return new ResponseEntity<>(strategyService.runCustomStrategyBacktest(inputDto), HttpStatus.OK);
    }*/
}
