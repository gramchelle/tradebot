package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.dto.indicators.*;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.service.DataCollectorService;
import lion.mode.tradebot_backend.service.TechnicalAnalysisService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class TechnicalAnalysisController { //Technical Analysis Endpoints

    private final DataCollectorService dataCollectorService;
    private final TechnicalAnalysisService technicalAnalysisService;

    @GetMapping("/stocks")
    public List<StockData> getStockData() {
        return dataCollectorService.getAllStockData();
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<List<StockData>> getStockDataBySymbol(@PathVariable String symbol) {
        return new ResponseEntity<List<StockData>>(dataCollectorService.getStockDataBySymbol(symbol), HttpStatus.OK);
    }

    @GetMapping("/rsi")
    public ResponseEntity<RSIResult> getRSIScore(@RequestParam String symbol) {
        RSIResult rsiResult = technicalAnalysisService.calculateRSI(symbol);
        return new ResponseEntity<>(rsiResult, HttpStatus.OK);
    }

}
