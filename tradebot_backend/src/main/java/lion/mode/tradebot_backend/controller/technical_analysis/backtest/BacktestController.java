package lion.mode.tradebot_backend.controller.technical_analysis.backtest;

import lion.mode.tradebot_backend.dto.indicator.params.RSIParams;
import lion.mode.tradebot_backend.model.Backtest;
import lion.mode.tradebot_backend.service.technicalanalysis.backtest.BacktestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
            @RequestParam(defaultValue = "1") int lookbackPeriod,
            @RequestParam(defaultValue = "close") String priceType,
            @RequestParam(defaultValue = "4.4") double calculationConfidence
            ){
        return new ResponseEntity<Backtest>(backtestService.rsiHistoricalBacktest(symbol, period, lowerLimit, upperLimit, date, lookback, lookbackPeriod, priceType, calculationConfidence), HttpStatus.OK);
    }

}
