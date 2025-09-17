package lion.mode.tradebot_backend.repository;

import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.model.StockDataPer30Mins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockDataPer30MinsRepository extends JpaRepository<StockDataPer30Mins, Long> {
    List<StockDataPer30Mins> findBySymbolOrderByTimestampAsc(String symbol);

    boolean existsBySymbolAndTimestamp(String symbol, LocalDateTime timestamp);

    List<StockDataPer30Mins> findBySymbol(String symbol);

    Optional<StockDataPer30Mins> findTopBySymbolOrderByTimestampDesc(String symbol);

    @Query("SELECT s FROM StockDataPer30Mins s WHERE s.symbol = ?1 ORDER BY s.timestamp DESC LIMIT ?2")
    List<StockDataPer30Mins> findTopNBySymbolOrderByTimestampDesc(String symbol, int n);

    @Query("SELECT s FROM StockDataPer30Mins s WHERE s.symbol = ?1 AND s.timestamp BETWEEN ?2 AND ?3 ORDER BY s.timestamp ASC")
    List<StockDataPer30Mins> findBySymbolAndTimestampBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);

    List<StockDataPer30Mins> findBySymbolAndTimestampBetweenOrderByTimestampAsc(String symbol, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM StockDataPer30Mins s WHERE s.symbol = ?1 AND s.timestamp <= ?2 ORDER BY s.timestamp DESC LIMIT 1")
    List<StockDataPer30Mins> findBySymbolAndTimestampLessThanEqualOrderByTimestampAsc(String symbol, LocalDateTime targetDate);

    @Query("SELECT DISTINCT s.symbol FROM StockDataPer30Mins s ORDER BY s.symbol ASC")
    List<String> findSymbols();

}
