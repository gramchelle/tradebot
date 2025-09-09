package lion.mode.tradebot_backend.service.fetchdata;

import lion.mode.tradebot_backend.model.Stock;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockDataService {

    private final StockDataRepository repository;

    public List<Stock> getAllStockData() {
        return repository.findAll();
    }

    public List<Stock> getStockDataBySymbol(String symbol) {
        return repository.findBySymbol(symbol);
    }

    public List<Stock> getStockDataBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findBySymbolAndTimestampBetween(symbol, startDate, endDate);
    }

    public List<String> getSymbols(){
        return repository.findSymbols();
    }

}
