package lion.mode.tradebot_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.BaseBacktestResponse;
import lion.mode.tradebot_backend.dto.indicator.RSIEntry;
import lion.mode.tradebot_backend.service.technicalanalysis.backtest.RSIBacktestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/technical-analysis/backtests")
@Tag(name = "Technical Analysis Backtests")
@RequiredArgsConstructor
public class BacktestController {

    private final RSIBacktestService rsiBacktestService;

    @GetMapping("/rsi")
    public ResponseEntity<BaseBacktestResponse> getBacktestResultsForRsi(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
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
        return new ResponseEntity<>(rsiBacktestService.runRsiBacktest(entry, lookback, horizon, interval, takeProfitThreshold, stopLossThreshold, tradingAmount), HttpStatus.OK);
    }

}
