package lion.mode.tradebot_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.base_responses.N_StrategyBacktestDto;
import lion.mode.tradebot_backend.service.technicalanalysis.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<N_StrategyBacktestDto> backtestRsi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runRsiStrategyBacktest(symbol, source, stopLoss, takeProfit, period, upperLimit, lowerLimit, lookback), HttpStatus.OK);
    }

    @GetMapping("/macd")
    public ResponseEntity<N_StrategyBacktestDto> backtestMacd(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "0.01") double stopLoss,
            @RequestParam(defaultValue = "0.02") double takeProfit,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runMacdStrategyBacktest(symbol, source, stopLoss, takeProfit, shortPeriod, longPeriod, signalPeriod, lookback), HttpStatus.OK);
    }

    @GetMapping("/dmi")
    public ResponseEntity<N_StrategyBacktestDto> backtestDmi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "20") int adxThreshold,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runDmiStrategyBacktest(symbol, source, stopLoss, takeProfit, period, adxThreshold, lookback), HttpStatus.OK);
    }

    @GetMapping("/mfi")
    public ResponseEntity<N_StrategyBacktestDto> backtestMfi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runMfiStrategyBacktest(symbol, source, stopLoss, takeProfit, period, upperLimit, lowerLimit, lookback), HttpStatus.OK);
    }

    @GetMapping("/ema-crossover")
    public ResponseEntity<N_StrategyBacktestDto> backtestEma(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "21") int longPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runEmaCrossoverStrategyBacktest(symbol, source, stopLoss, takeProfit, shortPeriod, longPeriod, lookback), HttpStatus.OK);
    }

    @GetMapping("/sma-crossover")
    public ResponseEntity<N_StrategyBacktestDto> backtestSma(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "21") int longPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runSmaCrossoverStrategyBacktest(symbol, source, stopLoss, takeProfit, shortPeriod, longPeriod, lookback), HttpStatus.OK);
    }

    @GetMapping("/bollinger-bands")
    public ResponseEntity<N_StrategyBacktestDto> backtestBollingerBands(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2") int stdDevMultiplier,
            @RequestParam(defaultValue = "SMA") String basisMaType,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "close") String source,
            @RequestParam(defaultValue = "252") int lookback
    ){
        return new ResponseEntity<>(strategyService.runBollingerBandsStrategyBacktest(symbol, source, stopLoss, takeProfit, period, stdDevMultiplier, basisMaType, lookback), HttpStatus.OK);

    }

    // Custom

    @GetMapping("/rsi-macd")
    public ResponseEntity<N_StrategyBacktestDto> backtestRsiAndMacd(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int rsiPeriod,
            @RequestParam(defaultValue = "70") int rsiUpperLimit,
            @RequestParam(defaultValue = "30") int rsiLowerLimit,
            @RequestParam(defaultValue = "0.05") double stopLoss,
            @RequestParam(defaultValue = "0.1") double takeProfit,
            @RequestParam(defaultValue = "12") int macdShort,
            @RequestParam(defaultValue = "26") int macdLong,
            @RequestParam(defaultValue = "9") int macdSignal,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runRsiMacdStrategyBacktest(symbol, source, stopLoss, takeProfit, rsiPeriod, rsiUpperLimit, rsiLowerLimit, macdShort, macdLong, macdSignal, lookback), HttpStatus.OK);
    
    }

    @GetMapping("/rsi-bollinger")
    public ResponseEntity<N_StrategyBacktestDto> backtestRsiAndBollinger(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int rsiPeriod,
            @RequestParam(defaultValue = "70") int rsiUpperLimit,
            @RequestParam(defaultValue = "30") int rsiLowerLimit,
            @RequestParam(defaultValue = "0.05") double stopLoss,
            @RequestParam(defaultValue = "0.1") double takeProfit,
            @RequestParam(defaultValue = "20") int bbPeriod,
            @RequestParam(defaultValue = "2") double stdDev,
            @RequestParam(defaultValue = "SMA") String maType,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runRsiBollingerStrategyBacktest(symbol, source, stopLoss, takeProfit, rsiPeriod, rsiUpperLimit, rsiLowerLimit, bbPeriod, stdDev, maType, lookback), HttpStatus.OK);
    }

    @GetMapping("/dmi-emacrossover")
    public ResponseEntity<N_StrategyBacktestDto> backtestDmiAndEmaCrossover(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int dmiPeriod,
            @RequestParam(defaultValue = "25") int adxThreshold,
            @RequestParam(defaultValue = "0.05") double stopLoss,
            @RequestParam(defaultValue = "0.1") double takeProfit,
            @RequestParam(defaultValue = "12") int emaShort,
            @RequestParam(defaultValue = "26") int emaLong,
            @RequestParam(defaultValue = "252") int lookback,
            @RequestParam(defaultValue = "close") String source
    ){
        return new ResponseEntity<>(strategyService.runDmiEmaStrategyBacktest(symbol, source, stopLoss, takeProfit, dmiPeriod, adxThreshold, emaShort, emaLong, lookback), HttpStatus.OK);
    }

}
