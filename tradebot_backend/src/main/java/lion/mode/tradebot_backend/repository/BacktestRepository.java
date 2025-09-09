package lion.mode.tradebot_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lion.mode.tradebot_backend.model.Backtest;

@Repository
public interface BacktestRepository extends JpaRepository<Backtest, Long>{

}
