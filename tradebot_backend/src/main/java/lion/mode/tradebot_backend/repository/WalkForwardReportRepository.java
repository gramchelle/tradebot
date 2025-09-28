package lion.mode.tradebot_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lion.mode.tradebot_backend.model.WalkForwardReport;

@Repository
public interface WalkForwardReportRepository extends JpaRepository<WalkForwardReport, Long> {

}
