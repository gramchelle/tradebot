package lion.mode.tradebot_backend.repository;

import lion.mode.tradebot_backend.model.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    Strategy findByStrategyName(String strategyName);
}
