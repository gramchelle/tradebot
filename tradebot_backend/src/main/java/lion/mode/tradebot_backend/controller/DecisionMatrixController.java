package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.LastDecisionResponse;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.MovingAveragesOverallResponse;
import lion.mode.tradebot_backend.service.technicalanalysis.DecisionMatrixService;
import lion.mode.tradebot_backend.service.technicalanalysis.WalkForwardOptimizationService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/decision-matrix")
@RequiredArgsConstructor
@Tag(name = "Decision Matrix", description = "Endpoints for retrieving decision matrix data")
public class DecisionMatrixController {

    private final DecisionMatrixService decisionMatrixService;
    private final WalkForwardOptimizationService walkForwardOptimizationService;

    @PostMapping("/decision-by-symbol")
    public ResponseEntity<LastDecisionResponse> runWalkforwardPost(@RequestBody WalkForwardRequestDto dto) {
        System.out.println("\n[INFO] Decision Matrix analysis started...");
        LastDecisionResponse response = decisionMatrixService.lastDecisionGenerator(dto);
        System.out.println("[INFO] Decision Matrix analysis completed.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/run-for-all-symbols")
    public ResponseEntity<List<LastDecisionResponse>> runWalkforwardForAllSymbols(@RequestBody WalkForwardRequestDto dto) {
        System.out.println("\n[INFO] Detailed Decision Matrix analysis started...");
        List<LastDecisionResponse> response = decisionMatrixService.lastDecisionResponseForAllSymbols(dto);
        System.out.println("[INFO] Detailed Decision Matrix analysis completed.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ma-overall/{symbol}")
    public ResponseEntity<MovingAveragesOverallResponse> overallMaSignal(@PathVariable String symbol) {
        System.out.println("\n[INFO] Overall MA Signal analysis started...");
        MovingAveragesOverallResponse response = walkForwardOptimizationService.getMovingAverages(symbol);
        System.out.println("[INFO] Overall MA Signal analysis completed.");
        return ResponseEntity.ok(response);
    }
}
