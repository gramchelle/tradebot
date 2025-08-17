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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityReturnValueHandler;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class StockDataController { //Technical Analysis Endpoints

    private final DataCollectorService dataCollectorService;
    private final TechnicalAnalysisService technicalAnalysisService;

    @GetMapping("/stocks")
    public List<StockData> getStockData() {
        return dataCollectorService.getAllStockData();
    }

    @GetMapping("/rsi/{symbol}")
    public ResponseEntity<RSIResult> getRSIScore(@PathVariable String symbol) {
        RSIResult rsiResult = technicalAnalysisService.calculateRSI(symbol);
        return new ResponseEntity<>(rsiResult, HttpStatus.OK);
    }

    @GetMapping("/macd/{symbol}")
    public ResponseEntity<MacdResult> getMACD(@PathVariable String symbol) {
        return new ResponseEntity<>(technicalAnalysisService.calculateMACD(symbol), HttpStatus.OK);
    }

    @GetMapping("/ma-crossover/{symbol}")
    public ResponseEntity<MACrossoverResult> getMACrossover(@PathVariable String symbol) {
        return new ResponseEntity<>(technicalAnalysisService.calculateMACrossover(symbol), HttpStatus.OK);
    }

    @GetMapping("/bollinger/{symbol}")
    public ResponseEntity<BollingerResult> getBollinger(@PathVariable String symbol) {
        return new ResponseEntity<>(technicalAnalysisService.calculateBollinger(symbol), HttpStatus.OK);
    }

    @GetMapping("/trend/{symbol}")
    public ResponseEntity<TrendlineResult> getTrend(@PathVariable String symbol) {
        return new ResponseEntity<>(technicalAnalysisService.calculateTrend(symbol), HttpStatus.OK);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<List<StockData>> getStockDataBySymbol(@PathVariable String symbol) {
        return new ResponseEntity<List<StockData>>(dataCollectorService.getStockDataBySymbol(symbol), HttpStatus.OK);
    }

    @GetMapping("/tech-analysis/{symbol}")
    public ResponseEntity<List> getTechnicalResultsBySymbol(@PathVariable String symbol) {
    List technicalResults = List.of(
            Map.of("rsi", technicalAnalysisService.calculateRSI(symbol)),
            Map.of("macd", technicalAnalysisService.calculateMACD(symbol).getTradeSignal()),
            Map.of("maCrossover", technicalAnalysisService.calculateMACrossover(symbol).getSignal()),
            Map.of("bollinger", technicalAnalysisService.calculateBollinger(symbol).getSignal()),
            Map.of("trend", technicalAnalysisService.calculateTrend(symbol).getTrend()) // TBD: getSlope yapılabilir
    );

        return new ResponseEntity<>(technicalResults, HttpStatus.OK);
    }
}
