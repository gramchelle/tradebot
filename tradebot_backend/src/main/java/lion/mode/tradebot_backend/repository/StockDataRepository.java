package lion.mode.tradebot_backend.repository;

import lion.mode.tradebot_backend.model.StockData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDataRepository extends JpaRepository<StockData, Long> {
    List<StockData> findBySymbolOrderByTimestampAsc(String symbol);

    boolean existsBySymbolAndTimestamp(String symbol, LocalDateTime timestamp);

    List<StockData> findBySymbol(String symbol);

    Optional<StockData> findTopBySymbolOrderByTimestampDesc(String symbol);

    @Query("SELECT s FROM StockData s WHERE s.symbol = ?1 ORDER BY s.timestamp DESC LIMIT ?2")
    List<StockData> findTopNBySymbolOrderByTimestampDesc(String symbol, int n);

    @Query("SELECT s FROM StockData s WHERE s.symbol = ?1 AND s.timestamp BETWEEN ?2 AND ?3 ORDER BY s.timestamp ASC")
    List<StockData> findBySymbolAndTimestampBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);

    List<StockData> findBySymbolAndTimestampBetweenOrderByTimestampAsc(String symbol, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM StockData s WHERE s.symbol = ?1 AND s.timestamp <= ?2 ORDER BY s.timestamp DESC LIMIT 1")
    List<StockData> findBySymbolAndTimestampLessThanEqualOrderByTimestampAsc(String symbol, LocalDateTime targetDate);

    @Query("SELECT DISTINCT s.symbol FROM StockData s ORDER BY s.symbol ASC")
    List<String> findSymbols();

}