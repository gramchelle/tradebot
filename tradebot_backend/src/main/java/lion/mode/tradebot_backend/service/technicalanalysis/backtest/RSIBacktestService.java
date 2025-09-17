package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import org.springframework.stereotype.Service;

import lion.mode.tradebot_backend.service.technicalanalysis.IndicatorService;

@Service
public class RSIBacktestService extends IndicatorService {

    public RSIBacktestService(StockDataRepository repository) {
        super(repository);
    }

    
}
