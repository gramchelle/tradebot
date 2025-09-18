package lion.mode.tradebot_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.service.technicalanalysis.backtest.BacktestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
@Tag(name = "FOR TESTING")
public class DummyController {

    private final BacktestService backtestService;

    @GetMapping("bollingerTest")
    public ResponseEntity<HashMap<String, Double>> getResult(@RequestParam String symbol){
        return new ResponseEntity<>(backtestService.calculateBollinger(symbol), HttpStatus.OK);
    }
}
