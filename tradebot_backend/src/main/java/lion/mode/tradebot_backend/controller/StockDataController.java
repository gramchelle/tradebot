package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.service.fetchdata.StockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class StockDataController {

    private final StockDataService stockDataService;

    @GetMapping("/stocks")
    public List<StockData> getStockData() {
        return stockDataService.getAllStockData();
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockData>> getStockDataBySymbol(@RequestParam String symbol) {
        return new ResponseEntity<List<StockData>>(stockDataService.getStockDataBySymbol(symbol), HttpStatus.OK);
    }

    @GetMapping("/symbols")
    public ResponseEntity<List<String>> getSymbols(){
        return new ResponseEntity<List<String>>(stockDataService.getSymbols(), HttpStatus.OK);
    }

}
