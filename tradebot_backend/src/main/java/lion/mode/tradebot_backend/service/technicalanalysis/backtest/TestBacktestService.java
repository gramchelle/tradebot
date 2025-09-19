package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;

@Service
public class TestBacktestService extends AbstractBacktestService {

    public TestBacktestService(StockDataRepository stockDataRepository, BacktestRepository backtestRepository) {
        super(stockDataRepository, backtestRepository);
    }
}
