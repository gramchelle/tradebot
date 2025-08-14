package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.service.DataCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class StockDataController {

    private final DataCollectorService dataCollectorService;

    @GetMapping("/stocks")
    public List<StockData> getStockData() {
        return dataCollectorService.fetchStockData();
    }
}
