package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.service.technicalanalysis.indicators.BollingerBandsService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicators.MACrossService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicators.MACDService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicators.RSIService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicators.TrendlineService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ta")
@RequiredArgsConstructor
public class TechnicalAnalysisController {

    private final RSIService rsiService;
    private final MACDService macdService;
    private final MACrossService macrossService;
    private final TrendlineService trendlineService;
    private final BollingerBandsService bollingerService;

/*

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllTechnicalIndicators(
            @RequestParam String symbol) {

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("RSI", rsiService.calculateRSI(symbol, 14, 14).getSignal());
        results.put("MACD", macdService.calculateMacd(symbol, 12, 26, 9, 1, 0.005).getSignal());
        results.put("MA-CROSS", macrossService.calculateEMACross(symbol, 9, 26).getSignal());
        results.put("BollingerBands", bollingerService.calculateAtDate(symbol, 20, 2.0, java.time.LocalDateTime.now()).getSignal());
        results.put("Trendline", trendlineService.calculateTrendline(symbol, 14, 50).getSignal());

        return new ResponseEntity<>(results, HttpStatus.OK);
    }
*/
}