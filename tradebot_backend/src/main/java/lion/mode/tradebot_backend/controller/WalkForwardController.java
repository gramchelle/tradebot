package lion.mode.tradebot_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto;
import lion.mode.tradebot_backend.model.WalkForwardReport;
import lion.mode.tradebot_backend.service.technicalanalysis.WalkForwardOptimizationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/walkforward")
@RequiredArgsConstructor
@Tag(name = "WalkForwardController", description = "Endpoints for Walk-Forward Optimization")
public class WalkForwardController {

    /* Walk-Forward Optimization Endpoints */

    private final WalkForwardOptimizationService walkForwardService;
    
    // Detailed walk-forward analysis returning train and validation reports
    @PostMapping("/detailed-run")
    public ResponseEntity<List<WalkForwardReport>> runDetailedWalkforward(@RequestBody WalkForwardRequestDto dto) {
        List<WalkForwardReport> reports = walkForwardService.runDetailedWalkForwardAnalysis(dto);
        return ResponseEntity.ok(reports);
    }

    // Returns only the latest walk-forward report
    @PostMapping("/run")
    public ResponseEntity<WalkForwardReport> runWalkforward(@RequestBody WalkForwardRequestDto dto) {
        WalkForwardReport report = walkForwardService.runWalkForwardAnalysis(dto);
        return ResponseEntity.ok(report);
    }
    
}
