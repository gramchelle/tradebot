package lion.mode.tradebot_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import lion.mode.tradebot_backend.model.StockDataIntraday;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockDataIntradayRepository extends JpaRepository<StockDataIntraday, Long> {
    List<StockDataIntraday> findBySymbolOrderByTimestampAsc(String symbol);

    boolean existsBySymbolAndTimestamp(String symbol, LocalDateTime timestamp);

    List<StockDataIntraday> findBySymbol(String symbol);

    Optional<StockDataIntraday> findTopBySymbolOrderByTimestampDesc(String symbol);

    @Query("SELECT s FROM StockDataIntraday s WHERE s.symbol = ?1 ORDER BY s.timestamp DESC LIMIT ?2")
    List<StockDataIntraday> findTopNBySymbolOrderByTimestampDesc(String symbol, int n);

    @Query("SELECT s FROM StockDataIntraday s WHERE s.symbol = ?1 AND s.timestamp BETWEEN ?2 AND ?3 ORDER BY s.timestamp ASC")
    List<StockDataIntraday> findBySymbolAndTimestampBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);

    List<StockDataIntraday> findBySymbolAndTimestampBetweenOrderByTimestampAsc(String symbol, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM StockDataIntraday s WHERE s.symbol = ?1 AND s.timestamp <= ?2 ORDER BY s.timestamp DESC LIMIT 1")
    List<StockDataIntraday> findBySymbolAndTimestampLessThanEqualOrderByTimestampAsc(String symbol, LocalDateTime targetDate);

    @Query("SELECT DISTINCT s.symbol FROM StockDataIntraday s ORDER BY s.symbol ASC")
    List<String> findSymbols();

}
