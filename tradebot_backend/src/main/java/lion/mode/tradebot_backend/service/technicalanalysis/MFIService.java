package lion.mode.tradebot_backend.service.technicalanalysis;

import org.springframework.stereotype.Service;

import lion.mode.tradebot_backend.repository.StockDataRepository;

@Service
public class MFIService extends IndicatorService {

    public MFIService(StockDataRepository repository) {
        super(repository);
    }
    
}
