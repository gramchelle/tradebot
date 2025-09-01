package lion.mode.tradebot_backend.controller.technical_analysis_controllers;

import lion.mode.tradebot_backend.service.technicalanalysis.BollingerService;
import lion.mode.tradebot_backend.service.technicalanalysis.MACrossService;
import lion.mode.tradebot_backend.service.technicalanalysis.MacdService;
import lion.mode.tradebot_backend.service.technicalanalysis.RsiService;
import lion.mode.tradebot_backend.service.technicalanalysis.TrendlineService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/ta")
@RequiredArgsConstructor
public class TechnicalAnalysisController {

    private final RsiService rsiService;
    private final MacdService macdService;
    private final MACrossService macrossService;
    private final TrendlineService trendlineService;
    private final BollingerService bollingerService;

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllTechnicalIndicators(
            @RequestParam String symbol) {

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("rsi", rsiService.calculateRSI(symbol, 14).getSignal());
        results.put("macd", macdService.calculateMacd(symbol, 12, 26, 9).getSignalText());
        results.put("maCross", macrossService.calculateMACross(symbol, 9, 26).getSignal());
        //results.put("bollingerMiddle", bollingerService.calculateBollinger(symbol, 20).getMiddle());
        results.put("trendSlope", trendlineService.calculateTrendline(symbol, 14).getTrendType());

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

}