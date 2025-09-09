package lion.mode.tradebot_backend.controller.technical_analysis.backtest;

import lion.mode.tradebot_backend.dto.backtest.StrategyDto;
import lion.mode.tradebot_backend.model.Strategy;
import lion.mode.tradebot_backend.service.technicalanalysis.backtest.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/strategy")
public class StrategyController {

    private final StrategyService strategyService;

    @PostMapping("/save")
    public ResponseEntity<String> saveStrategy(@RequestBody StrategyDto strategyDto){
        boolean isCreated = strategyService.createStrategy(strategyDto);
        if (isCreated) return new ResponseEntity<String>("Strategy saving is successful!", HttpStatus.OK);
        else return new ResponseEntity<String>("Strategy couldn't get created.", HttpStatus.BAD_REQUEST);
    }
}
