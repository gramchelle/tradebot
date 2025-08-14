package lion.mode.tradebot_backend.repository;

import lion.mode.tradebot_backend.model.StockData;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDataRepository extends JpaRepository<StockData, Long> {

    @Query("SELECT sd FROM StockData sd WHERE sd.symbol = ?1 ORDER BY sd.timestamp ASC")
    List<StockData> findBySymbolOrderByTimestampAsc(String symbol);

    boolean existsBySymbolAndTimestamp(String symbol, LocalDateTime timestamp);

}