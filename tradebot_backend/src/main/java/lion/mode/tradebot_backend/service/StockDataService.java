package lion.mode.tradebot_backend.service;

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

    // TODO: Belirli bir hisse senedi ve tarih aralığı için OHLCV verisini veritabanından çeken bir service katmanı yazılmalıdır.
    public List<StockData> getStockDataBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findBySymbolAndTimestampBetween(symbol, startDate, endDate);
    }
}
