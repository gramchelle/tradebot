package lion.mode.tradebot_backend.controller.technicalanalysis;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.base_responses.BaseBacktestResponse;
import lion.mode.tradebot_backend.dto.indicator_entry.*;
import lion.mode.tradebot_backend.service.technicalanalysis.backtest.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/technical-analysis/backtests")
@RequiredArgsConstructor
@Tag(name = "Technical Analysis Backtests")
public class BacktestController {

    private final RSIBacktestService rsiBacktestService;
    private final MACDBacktestService macdBacktestService;
    private final MACrossoverBacktestService macrossoverBacktestService;
    private final BollingerBandsBacktestService bollingerBacktestService;
    private final DMIBacktestService dmiBacktestService;
    private final MFIBacktestService mfiBacktestService;

    @GetMapping("/rsi")
    public ResponseEntity<BaseBacktestResponse> runRsiBacktest(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "250") int lookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "0.02") double calculationConfidence,
            @RequestParam(defaultValue = "70") int upperLimit,
            @RequestParam(defaultValue = "30") int lowerLimit,
            @RequestParam(defaultValue = "close") String source,
            @RequestParam(defaultValue = "1d") String interval,
            @RequestParam(defaultValue = "1") int tradingAmount,
            @RequestParam(defaultValue = "0.3") double takeProfitThreshold,
            @RequestParam(defaultValue = "0.1") double stopLossThreshold) {
        RSIEntry entry = new RSIEntry(symbol, date, period, upperLimit, lowerLimit, source);
        return new ResponseEntity<>(rsiBacktestService.runBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount), HttpStatus.OK);
    }

    @GetMapping("/macd")
    public ResponseEntity<BaseBacktestResponse> calculateMACD(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "12") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "250") int lookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "5") int histogramTrendPeriod,
            @RequestParam(defaultValue = "0.00005") double histogramConfidence,
            @RequestParam(defaultValue = "close") String source,
            @RequestParam(defaultValue = "1d") String interval,
            @RequestParam(defaultValue = "1") int tradingAmount,
            @RequestParam(defaultValue = "0.3") double takeProfitThreshold,
            @RequestParam(defaultValue = "0.1") double stopLossThreshold) {
        MACDEntry entry = new MACDEntry(symbol, date, shortPeriod, longPeriod, signalPeriod, histogramTrendPeriod, histogramConfidence, source);
        return new ResponseEntity<>(macdBacktestService.runBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount), HttpStatus.OK);
    }

    @GetMapping("/ma-crossover")
    public ResponseEntity<BaseBacktestResponse> calculateEmaCrossover(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "250") int lookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "EMA") String maType,
            @RequestParam(defaultValue = "5") int maCrossoverLookback,
            @RequestParam(defaultValue = "close") String source,
            @RequestParam(defaultValue = "0.05") double relativeThreshold,
            @RequestParam(defaultValue = "1d") String interval,
            @RequestParam(defaultValue = "1") int tradingAmount,
            @RequestParam(defaultValue = "0.3") double takeProfitThreshold,
            @RequestParam(defaultValue = "0.1") double stopLossThreshold){
        MACrossoverEntry entry = new MACrossoverEntry(symbol, date, maCrossoverLookback, shortPeriod, longPeriod, source, maType, relativeThreshold);
        return new ResponseEntity<>(macrossoverBacktestService.runBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount), HttpStatus.OK);
    }

    @GetMapping("/bollinger-bands")
    public ResponseEntity<BaseBacktestResponse> calculateBollingerBands(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "20") int period,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "250") int lookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "2.0") double numberOfDeviations,
            @RequestParam(defaultValue = "5.0") double squeezeConfidence,
            @RequestParam(defaultValue = "close") String source,
            @RequestParam(defaultValue = "1d") String interval,
            @RequestParam(defaultValue = "1") int tradingAmount,
            @RequestParam(defaultValue = "0.3") double takeProfitThreshold,
            @RequestParam(defaultValue = "0.1") double stopLossThreshold) {
        BollingerBandsEntry entry = new BollingerBandsEntry(symbol, period, date, numberOfDeviations, source, squeezeConfidence);
        return new ResponseEntity<>(bollingerBacktestService.runBollingerBandsBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount), HttpStatus.OK);
    }

    @GetMapping("/mfi")
    public ResponseEntity<BaseBacktestResponse> calculateMFI(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "250") int lookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "20") int lowerLimit,
            @RequestParam(defaultValue = "80") int upperLimit,
            @RequestParam(defaultValue = "1d") String interval,
            @RequestParam(defaultValue = "1") int tradingAmount,
            @RequestParam(defaultValue = "0.3") double takeProfitThreshold,
            @RequestParam(defaultValue = "0.1") double stopLossThreshold) {
        MFIEntry entry = new MFIEntry(symbol, date, period, upperLimit, lowerLimit);
        return new ResponseEntity<>(mfiBacktestService.runBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount), HttpStatus.OK);
    }

    @GetMapping("/dmi")
    public ResponseEntity<BaseBacktestResponse> calculateDMI(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "#{T(java.time.Instant).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(defaultValue = "250") int lookback,
            @RequestParam(defaultValue = "2") int horizon,
            @RequestParam(defaultValue = "25") double strongTrendThreshold,
            @RequestParam(defaultValue = "20") double moderateTrendThreshold,
            @RequestParam(defaultValue = "3.0") double diDiff,
            @RequestParam(defaultValue = "1d") String interval,
            @RequestParam(defaultValue = "1") int tradingAmount,
            @RequestParam(defaultValue = "0.3") double takeProfitThreshold,
            @RequestParam(defaultValue = "0.1") double stopLossThreshold){
        DMIEntry entry = new DMIEntry(symbol, period, date, strongTrendThreshold, moderateTrendThreshold, diDiff);
        return new ResponseEntity<>(dmiBacktestService.runDmiBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount), HttpStatus.OK);
    }

}
