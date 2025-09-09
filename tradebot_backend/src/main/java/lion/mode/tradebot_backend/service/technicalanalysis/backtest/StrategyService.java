package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.dto.backtest.StrategyDto;
import lion.mode.tradebot_backend.mapper.StrategyMapper;
import lion.mode.tradebot_backend.model.Strategy;
import lion.mode.tradebot_backend.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepository;
    private StrategyMapper strategyMapper;

    public boolean createStrategy(StrategyDto strategyDto){
        try {
            Strategy strategy = StrategyMapper.toEntity(strategyDto);
            strategyRepository.save(strategy);
            System.out.println("[✓] Strategy '" + strategyDto.getName() + "' is saved successfully.");
            return true;
        } catch (Exception e){
            System.out.println("[!] An error occurred during creating strategy: " + e.getMessage());
            return false;
        }
    }

    public Strategy getStrategyById(Long id){
        return strategyRepository.findById(id).orElse(null);
    }

    public List<Strategy> getAllStrategies(){
        return strategyRepository.findAll();
    }


}
