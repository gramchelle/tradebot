package lion.mode.tradebot_backend.controller.technicalanalysis;

import java.time.Instant;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.indicator_entry.*;
import lion.mode.tradebot_backend.service.technicalanalysis.backtest.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/technical-analysis/backtest-logs")
@Tag(name = "Technical Analysis Backtest Logs")
@RequiredArgsConstructor
public class BacktestLogController {

    private final RSIBacktestService rsiBacktestService;
    private final MACDBacktestService macdBacktestService;
    private final MACrossoverBacktestService macrossoverBacktestService;
    private final BollingerBandsBacktestService bollingerBacktestService;
    private final DMIBacktestService dmiBacktestService;
    private final MFIBacktestService mfiBacktestService;

    @PostMapping("/saveRsiParams")
    public ResponseEntity<String> saveRsiBacktestParams(
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
        boolean isSaved = rsiBacktestService.saveIndicatorBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount);
        if (isSaved) return new ResponseEntity<>("RSI backtest parameters saved successfully.", HttpStatus.OK);
        else return new ResponseEntity<>("Failed to save RSI backtest parameters.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/saveMacdParams")
    public ResponseEntity<String> calculateMACD(
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
        boolean isSaved = macdBacktestService.saveIndicatorBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount);
        if (isSaved) return new ResponseEntity<>("MACD backtest parameters saved successfully.", HttpStatus.OK);
        else return new ResponseEntity<>("Failed to save MACD backtest parameters.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/saveMaCrossoverParams")
    public ResponseEntity<String> calculateEmaCrossover(
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
            @RequestParam(defaultValue = "0.1") double stopLossThreshold) {
        MACrossoverEntry entry = new MACrossoverEntry(symbol, date, maCrossoverLookback, shortPeriod, longPeriod, source, maType, relativeThreshold);
        boolean isSaved = macrossoverBacktestService.saveIndicatorBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount);
        if (isSaved) return new ResponseEntity<>("MA Crossover backtest parameters saved successfully.", HttpStatus.OK);
        else return new ResponseEntity<>("Failed to save MA Crossover backtest parameters.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/saveBollingerBandsParams")
    public ResponseEntity<String> saveBollingerBandsBacktestParams(
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
        boolean isSaved = bollingerBacktestService.saveBollingerBandsIndicatorBacktestResults(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount);
        if (isSaved) return new ResponseEntity<>("Bollinger Bands backtest parameters saved successfully.", HttpStatus.OK);
        else return new ResponseEntity<>("Failed to save Bollinger Bands backtest parameters.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/saveDmiParams")
    public ResponseEntity<String> saveDmiBacktestParams(
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
            @RequestParam(defaultValue = "0.1") double stopLossThreshold) {
        DMIEntry entry = new DMIEntry(symbol, period, date, strongTrendThreshold, moderateTrendThreshold, diDiff);
        boolean isSaved = dmiBacktestService.saveDmiIndicatorBacktest(entry, lookback, "DMI", horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount);
        if (isSaved) return new ResponseEntity<>("DMI backtest parameters saved successfully.", HttpStatus.OK);
        else return new ResponseEntity<>("Failed to save DMI backtest parameters.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/saveMfiParams")
    public ResponseEntity<String> saveMfiBacktestParams(
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
        boolean isSaved = mfiBacktestService.saveIndicatorBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount);
        if (isSaved) return new ResponseEntity<>("MFI backtest parameters saved successfully.", HttpStatus.OK);
        else return new ResponseEntity<>("Failed to save MFI backtest parameters.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
