package lion.mode.tradebot_backend.service.technicalanalysis;

import org.springframework.stereotype.Service;

import lion.mode.tradebot_backend.repository.StockDataRepository;

@Service
public class ADXService extends IndicatorService{

    public ADXService(StockDataRepository repository) {
        super(repository);
    }
}
