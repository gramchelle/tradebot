package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;

@Service
public class MacdService extends AbstractIndicatorService {

    public MacdService(StockDataRepository stockDataRepository) {
        super(stockDataRepository);
    }



}
