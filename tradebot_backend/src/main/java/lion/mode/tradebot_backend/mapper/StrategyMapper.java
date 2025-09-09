package lion.mode.tradebot_backend.mapper;

import lion.mode.tradebot_backend.dto.backtest.StrategyDto;
import lion.mode.tradebot_backend.model.Strategy;

public class StrategyMapper {

    public static StrategyDto toDto(Strategy strategy){
        StrategyDto dto = new StrategyDto();
        dto.setName(strategy.getName());
        dto.setIndicator(strategy.getIndicator());
        // dto.setParams(strategy.getParams());
        return dto;
    }

    public static Strategy toEntity(StrategyDto dto){
        Strategy strategy = new Strategy();
        strategy.setName(dto.getName());
        strategy.setIndicator(dto.getIndicator());
        // strategy.setParams(dto.getParams());
        return strategy;
    }

}
