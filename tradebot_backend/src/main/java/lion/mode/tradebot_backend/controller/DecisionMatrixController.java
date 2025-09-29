package lion.mode.tradebot_backend.controller;

import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDto;
import lion.mode.tradebot_backend.dto.technicalanalysis.request.WalkForwardRequestDtoNoSymbol;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.AllSymbolsBacktestResult;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.LastDecisionResponse;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.MovingAveragesOverallResponse;
import lion.mode.tradebot_backend.dto.technicalanalysis.response.StrategyBacktestDto;
import lion.mode.tradebot_backend.model.WalkForwardReport;
import lion.mode.tradebot_backend.service.technicalanalysis.DecisionMatrixService;
import lion.mode.tradebot_backend.service.technicalanalysis.WalkForwardOptimizationService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
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

    @Deprecated
    @GetMapping("/all-indicators")
    public ResponseEntity<StrategyBacktestDto> backtestAllIndicators(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int rsiPeriod,
            @RequestParam(defaultValue = "14") int mfiPeriod,
            @RequestParam(defaultValue = "12") int macdShort,
            @RequestParam(defaultValue = "26") int macdLong,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "14") int dmiPeriod,
            @RequestParam(defaultValue = "25") int adxThreshold,
            @RequestParam(defaultValue = "9") int emaShort,
            @RequestParam(defaultValue = "21") int emaLong,
            @RequestParam(defaultValue = "20") int bbPeriod,
            @RequestParam(defaultValue = "2") double bbStdDev,
            @RequestParam(defaultValue = "50") int smaPeriod,
            @RequestParam(defaultValue = "200") int smaLongPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            // @RequestParam(defaultValue = "0") int isTrailingStopLoss,
            @RequestParam(defaultValue = "1000") int lookback,
            @RequestParam(defaultValue = "close") String source
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("rsiPeriod", rsiPeriod);
        params.put("mfiPeriod", mfiPeriod);
        params.put("macdShort", macdShort);
        params.put("macdLong", macdLong);
        params.put("signalPeriod", signalPeriod);
        params.put("dmiPeriod", dmiPeriod);
        params.put("adxThreshold", adxThreshold);
        params.put("emaShort", emaShort);
        params.put("emaLong", emaLong);
        params.put("bbPeriod", bbPeriod);
        params.put("bbStdDev", bbStdDev);
        params.put("smaPeriod", smaPeriod);
        params.put("smaLongPeriod", smaLongPeriod);

        return new ResponseEntity<>(decisionMatrixService.calculateDecisionMatrix(symbol, source, stopLoss, takeProfit, params, lookback),HttpStatus.OK);
    }

    @Deprecated
    @GetMapping("/all-symbols-all-indicators")
    public ResponseEntity<List<AllSymbolsBacktestResult>> backtestAllIndicatorsAllSymbols(
            @RequestParam(defaultValue = "14") int rsiPeriod,
            @RequestParam(defaultValue = "14") int mfiPeriod,
            @RequestParam(defaultValue = "12") int macdShort,
            @RequestParam(defaultValue = "26") int macdLong,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "14") int dmiPeriod,
            @RequestParam(defaultValue = "25") int adxThreshold,
            @RequestParam(defaultValue = "9") int emaShort,
            @RequestParam(defaultValue = "21") int emaLong,
            @RequestParam(defaultValue = "20") int bbPeriod,
            @RequestParam(defaultValue = "2") double bbStdDev,
            @RequestParam(defaultValue = "50") int smaPeriod,
            @RequestParam(defaultValue = "200") int smaLongPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "1000") int lookback,
            @RequestParam(defaultValue = "close") String source
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("rsiPeriod", rsiPeriod);
        params.put("mfiPeriod", mfiPeriod);
        params.put("macdShort", macdShort);
        params.put("macdLong", macdLong);
        params.put("signalPeriod", signalPeriod);
        params.put("dmiPeriod", dmiPeriod);
        params.put("adxThreshold", adxThreshold);
        params.put("emaShort", emaShort);
        params.put("emaLong", emaLong);
        params.put("bbPeriod", bbPeriod);
        params.put("bbStdDev", bbStdDev);
        params.put("smaPeriod", smaPeriod);
        params.put("smaLongPeriod", smaLongPeriod);

        return new ResponseEntity<List<AllSymbolsBacktestResult>>(decisionMatrixService.runForAllSymbols(source, stopLoss, takeProfit, params, lookback),HttpStatus.OK);
    }

    @Deprecated
    @GetMapping("/overall-technical-signal")
    public ResponseEntity<AllSymbolsBacktestResult> overallTechnicalSignal(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "14") int rsiPeriod,
            @RequestParam(defaultValue = "14") int mfiPeriod,
            @RequestParam(defaultValue = "12") int macdShort,
            @RequestParam(defaultValue = "26") int macdLong,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "14") int dmiPeriod,
            @RequestParam(defaultValue = "25") int adxThreshold,
            @RequestParam(defaultValue = "9") int emaShort,
            @RequestParam(defaultValue = "21") int emaLong,
            @RequestParam(defaultValue = "20") int bbPeriod,
            @RequestParam(defaultValue = "2") double bbStdDev,
            @RequestParam(defaultValue = "50") int smaPeriod,
            @RequestParam(defaultValue = "200") int smaLongPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "1000") int lookback,
            @RequestParam(defaultValue = "close") String source
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("rsiPeriod", rsiPeriod);
        params.put("mfiPeriod", mfiPeriod);
        params.put("macdShort", macdShort);
        params.put("macdLong", macdLong);
        params.put("signalPeriod", signalPeriod);
        params.put("dmiPeriod", dmiPeriod);
        params.put("adxThreshold", adxThreshold);
        params.put("emaShort", emaShort);
        params.put("emaLong", emaLong);
        params.put("bbPeriod", bbPeriod);
        params.put("bbStdDev", bbStdDev);
        params.put("smaPeriod", smaPeriod);
        params.put("smaLongPeriod", smaLongPeriod); 
        return new ResponseEntity<>(decisionMatrixService.runOverallTechnicalSignal(symbol, source, stopLoss, takeProfit, params, lookback), HttpStatus.OK);
    }

    @Deprecated
    @GetMapping("/technical-analysis/all-symbols")
    public ResponseEntity<List<AllSymbolsBacktestResult>> backtestAllIndicatorsAndMaAllSymbols(
            @RequestParam(defaultValue = "14") int rsiPeriod,
            @RequestParam(defaultValue = "14") int mfiPeriod,
            @RequestParam(defaultValue = "12") int macdShort,
            @RequestParam(defaultValue = "26") int macdLong,
            @RequestParam(defaultValue = "9") int signalPeriod,
            @RequestParam(defaultValue = "14") int dmiPeriod,
            @RequestParam(defaultValue = "25") int adxThreshold,
            @RequestParam(defaultValue = "9") int emaShort,
            @RequestParam(defaultValue = "21") int emaLong,
            @RequestParam(defaultValue = "20") int bbPeriod,
            @RequestParam(defaultValue = "2") double bbStdDev,
            @RequestParam(defaultValue = "50") int smaPeriod,
            @RequestParam(defaultValue = "200") int smaLongPeriod,
            @RequestParam(defaultValue = "1.0") double stopLoss,
            @RequestParam(defaultValue = "2.0") double takeProfit,
            @RequestParam(defaultValue = "1000") int lookback,
            @RequestParam(defaultValue = "close") String source
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("rsiPeriod", rsiPeriod);
        params.put("mfiPeriod", mfiPeriod);
        params.put("macdShort", macdShort);
        params.put("macdLong", macdLong);
        params.put("signalPeriod", signalPeriod);
        params.put("dmiPeriod", dmiPeriod);
        params.put("adxThreshold", adxThreshold);
        params.put("emaShort", emaShort);
        params.put("emaLong", emaLong);
        params.put("bbPeriod", bbPeriod);
        params.put("bbStdDev", bbStdDev);
        params.put("smaPeriod", smaPeriod);
        params.put("smaLongPeriod", smaLongPeriod);

        return new ResponseEntity<List<AllSymbolsBacktestResult>>(decisionMatrixService.runTechnicalAnalysisForAllSymbols(source, stopLoss, takeProfit, params, lookback),HttpStatus.OK);
    }

    /// NEW Endpoints

    @PostMapping("/decision-by-symbol")
    public ResponseEntity<LastDecisionResponse> runWalkforwardPost(@RequestBody WalkForwardRequestDto dto) {
        System.out.println("\n[INFO] Decision Matrix analysis started...");
        LastDecisionResponse response = decisionMatrixService.lastDecisionGenerator(dto);
        System.out.println("[INFO] Decision Matrix analysis completed.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/run-for-all-symbols")
    public ResponseEntity<List<LastDecisionResponse>> runWalkforwardForAllSymbols(@RequestBody WalkForwardRequestDtoNoSymbol dto) {
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
