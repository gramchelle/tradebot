package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.service.DataCollectorService;
import lion.mode.tradebot_backend.service.TechnicalAnalysisService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityReturnValueHandler;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class StockDataController {

    private final DataCollectorService dataCollectorService;
    private final TechnicalAnalysisService technicalAnalysisService;

    @GetMapping("/stocks")
    public List<StockData> getStockData() {
        return dataCollectorService.getAllStockData();
    }

    @GetMapping("/rsi/{symbol}")
    public ResponseEntity<Map<String, Object>> getRSIScore(@PathVariable String symbol) {
        double rsi = technicalAnalysisService.calculateRSI(symbol);

        return new ResponseEntity<>(Map.of(
                "symbol", symbol,
                "rsi", rsi,
                "signal", rsi == 1.0 ? "buy" : rsi == -1.0 ? "sell" : "hold"
        ), HttpStatus.OK);
    }

}

