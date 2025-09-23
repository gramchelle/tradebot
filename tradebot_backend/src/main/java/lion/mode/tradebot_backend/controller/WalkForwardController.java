package lion.mode.tradebot_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.base_responses.N_BacktestReport;
import lion.mode.tradebot_backend.service.N_technicalanalysis.WalkForwardOptimization;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/walkforward")
@RequiredArgsConstructor
@Tag(name = "WalkForwardController", description = "Endpoints for walk-forward optimization")
public class WalkForwardController {

    private final WalkForwardOptimization walkForwardService;

    @GetMapping("/custom-confluence")
    public ResponseEntity<N_BacktestReport> runRsiWalkforwardOptimization(@RequestParam String symbol) {
        return ResponseEntity.ok(walkForwardService.runCustomConfluenceStrategy(symbol));
    }
}