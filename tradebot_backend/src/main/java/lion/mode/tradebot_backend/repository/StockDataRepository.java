package lion.mode.tradebot_backend.repository;

import lion.mode.tradebot_backend.model.StockDataDaily;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDataRepository extends JpaRepository<StockDataDaily, Long> {
    List<StockDataDaily> findBySymbolOrderByTimestampAsc(String symbol);

    boolean existsBySymbolAndTimestamp(String symbol, LocalDateTime timestamp);

    List<StockDataDaily> findBySymbol(String symbol);

    Optional<StockDataDaily> findTopBySymbolOrderByTimestampDesc(String symbol);

    @Query("SELECT s FROM StockDataDaily s WHERE s.symbol = ?1 ORDER BY s.timestamp DESC LIMIT ?2")
    List<StockDataDaily> findTopNBySymbolOrderByTimestampDesc(String symbol, int n);

    @Query("SELECT s FROM StockDataDaily s WHERE s.symbol = ?1 AND s.timestamp BETWEEN ?2 AND ?3 ORDER BY s.timestamp ASC")
    List<StockDataDaily> findBySymbolAndTimestampBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);

    List<StockDataDaily> findBySymbolAndTimestampBetweenOrderByTimestampAsc(String symbol, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM StockDataDaily s WHERE s.symbol = ?1 AND s.timestamp <= ?2 ORDER BY s.timestamp DESC LIMIT 1")
    List<StockDataDaily> findBySymbolAndTimestampLessThanEqualOrderByTimestampAsc(String symbol, LocalDateTime targetDate);

    @Query("SELECT DISTINCT s.symbol FROM StockDataDaily s ORDER BY s.symbol ASC")
    List<String> findSymbols();

}