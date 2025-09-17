package lion.mode.tradebot_backend.controller.technical_analysis.backtest;

import lion.mode.tradebot_backend.dto.allResults;
import lion.mode.tradebot_backend.model.Backtest;
import lion.mode.tradebot_backend.service.technicalanalysis.backtest.BacktestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ta/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final BacktestService backtestService;

    @GetMapping("/rsi/{symbol}")
    public ResponseEntity<Backtest> getBacktestResultsForRSI(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "14") int lookback,
            @RequestParam(defaultValue = "1") int horizon,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "0.01") double calculationConfidence
            ){
        return new ResponseEntity<Backtest>(backtestService.rsiHistoricalBacktest(symbol, period, lowerLimit, upperLimit, date, lookback, horizon, priceType, calculationConfidence), HttpStatus.OK);
    }

    @GetMapping("/ema-cross/{symbol}")
    public ResponseEntity<Backtest> getBacktestResultsForEMACross(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "25") int shortPeriod,
            @RequestParam(defaultValue = "75") int longPeriod,
            @RequestParam(defaultValue = "5") int lookback,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "20") int backtestLookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "0.01") double calculationConfidence
    ){
        return new ResponseEntity<Backtest>(backtestService.emaCrossoverHistoricalBacktest(symbol, shortPeriod, longPeriod, lookback, date, priceType, backtestLookback, horizon, calculationConfidence), HttpStatus.OK);
    }

    @GetMapping("/sma-cross/{symbol}")
    public ResponseEntity<Backtest> getBacktestResultsForSMACross(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "50") int shortPeriod,
            @RequestParam(defaultValue = "200") int longPeriod,
            @RequestParam(defaultValue = "5") int lookback,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "20") int backtestLookback,
            @RequestParam(defaultValue = "2") int lookbackPeriod,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "0.1") double calculationConfidence
    ){
        return new ResponseEntity<Backtest>(backtestService.smaCrossoverHistoricalBacktest(symbol, shortPeriod, longPeriod, lookback, date, priceType, backtestLookback, lookbackPeriod, calculationConfidence), HttpStatus.OK);
    }

    @GetMapping("/macd/{symbol}")
    public ResponseEntity<Backtest> getBacktestResultsForMACD(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "5") int lookback,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "20") int backtestLookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "0.01") double calculationConfidence
    ){
        return new ResponseEntity<Backtest>(backtestService.macdHistoricalBacktest(symbol, shortPeriod, longPeriod, signalPeriod, date, priceType, backtestLookback, horizon, calculationConfidence), HttpStatus.OK);
    }

    @GetMapping("/bollinger/{symbol}")
    public ResponseEntity<Backtest> getBacktestResultsForBollinger(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "2.0") double nbDev,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "1.0") double squeezeConfidence,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "20") int backtestLookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "0.01") double calculationConfidence
    ){
        return new ResponseEntity<Backtest>(backtestService.bollingerHistoricalBacktest(symbol, period, nbDev, date, squeezeConfidence, priceType, backtestLookback, horizon, calculationConfidence), HttpStatus.OK);
    }

    // TEST EDİLECEK

    @GetMapping("/trendline/{symbol}")
    public ResponseEntity<Backtest> getBacktestResultsForTrendline(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "5") int lookback,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "0.01") double calculationConfidence,
            @RequestParam(defaultValue = "0.01") double slopeConfidence,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "20") int backtestLookback,
            @RequestParam(defaultValue = "2") int horizon
    ){
        return new ResponseEntity<Backtest>(backtestService.trendlineHistoricalBacktest(symbol, period, date, lookback, slopeConfidence, priceType, backtestLookback, horizon, calculationConfidence), HttpStatus.OK);
    }

    @GetMapping("/dmi/{symbol}")
    public ResponseEntity<Backtest> getBacktestResultsForDMI(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "25.0") double adxThreshold,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "14") int lookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "0.01") double calculationConfidence,
            @RequestParam(defaultValue = "20") int backtestLookback
    ){
        return new ResponseEntity<Backtest>(backtestService.dmiHistoricalBacktest(symbol, period, date, priceType, backtestLookback, horizon, calculationConfidence), HttpStatus.OK);
    }

    @GetMapping("/mfi/{symbol}")
    public ResponseEntity<Backtest> getBacktestResultsForMFI(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "80") int upperLimit,
            @RequestParam(defaultValue = "20") int lowerLimit,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "14") int lookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "0.01") double calculationConfidence,
            @RequestParam(defaultValue = "20") int backtestLookback
    ){
        return new ResponseEntity<Backtest>(backtestService.mfiHistoricalBacktest(symbol, period, date, lowerLimit, upperLimit, priceType, backtestLookback, horizon, calculationConfidence), HttpStatus.OK);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<List<allResults>> getAll(
        @PathVariable String symbol,
        @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
        @RequestParam(defaultValue = "400") int lookback,
        @RequestParam(defaultValue = "4") int horizon,
        @RequestParam(defaultValue = "close") String priceType,
        @RequestParam(defaultValue = "0.01") double calculationConfidence
    ){
        List<allResults> results = backtestService.decisionMatrix(symbol, date, lookback, horizon, priceType, calculationConfidence);
        return new ResponseEntity<List<allResults>>(results, HttpStatus.OK);
    }

}
