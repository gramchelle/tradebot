package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.service.technicalanalysis.BollingerService;
import lion.mode.tradebot_backend.service.technicalanalysis.MACrossService;
import lion.mode.tradebot_backend.service.technicalanalysis.MacdService;
import lion.mode.tradebot_backend.service.technicalanalysis.RsiService;
import lion.mode.tradebot_backend.service.technicalanalysis.TrendlineService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        results.put("rsi", rsiService.calculateRSI(symbol, 14, 14).getSignal());
        results.put("macd", macdService.calculateMacd(symbol, 12, 26, 9, 1, 0.005).getSignal());
        results.put("maCross", macrossService.calculateEMACross(symbol, 9, 26).getSignal());
        results.put("bollingerMiddle", bollingerService.calculateLatest(symbol, 20, 2.0).getSignal());
        results.put("trendSlope", trendlineService.calculateTrendline(symbol, 14, 50).getSignal());

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

}