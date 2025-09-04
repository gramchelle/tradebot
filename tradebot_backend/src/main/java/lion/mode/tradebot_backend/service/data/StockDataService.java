package lion.mode.tradebot_backend.service.data;

import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockDataService {

    private final StockDataRepository repository;

    public List<StockData> getAllStockData() {
        return repository.findAll();
    }

    public List<StockData> getStockDataBySymbol(String symbol) {
        return repository.findBySymbol(symbol);
    }

    public List<StockData> getStockDataBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findBySymbolAndTimestampBetween(symbol, startDate, endDate);
    }

    public List<String> getSymbols(){
        return repository.findSymbols();
    }

}
