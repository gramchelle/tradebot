package lion.mode.tradebot_backend.service.technicalanalysis.strategy;

import lion.mode.tradebot_backend.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepository;

}