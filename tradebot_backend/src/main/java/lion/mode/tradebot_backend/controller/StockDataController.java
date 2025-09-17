package lion.mode.tradebot_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.service.data.StockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "Stock Data", description = "Get Endpoints for Stock Data")
public class StockDataController {

    private final StockDataService stockDataService;

    @GetMapping("/stocks")
    public List<StockDataDaily> getStockData() {
        return stockDataService.getAllStockData();
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockDataDaily>> getStockDataBySymbol(@RequestParam String symbol) {
        return new ResponseEntity<List<StockDataDaily>>(stockDataService.getStockDataBySymbol(symbol), HttpStatus.OK);
    }

    @GetMapping("/symbols")
    public ResponseEntity<List<String>> getSymbols(){
        return new ResponseEntity<List<String>>(stockDataService.getSymbols(), HttpStatus.OK);
    }

}
