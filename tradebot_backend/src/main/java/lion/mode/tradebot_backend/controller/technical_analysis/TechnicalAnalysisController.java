package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.service.technicalanalysis.BollingerBandsService;
import lion.mode.tradebot_backend.service.technicalanalysis.DMIService;
import lion.mode.tradebot_backend.service.technicalanalysis.MACrossService;
import lion.mode.tradebot_backend.service.technicalanalysis.MFIService;
import lion.mode.tradebot_backend.service.technicalanalysis.MACDService;
import lion.mode.tradebot_backend.service.technicalanalysis.RSIService;
import lion.mode.tradebot_backend.service.technicalanalysis.TrendlineService;
import lombok.RequiredArgsConstructor;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final DMIService dmiService;
    private final MFIService mfiService;

    @GetMapping("/getSignals/{symbol}")
    public ResponseEntity<Dictionary<String, Integer>> getAllTechnicalIndicators(
        @PathVariable String symbol,
        @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
        ) {

        Dictionary<String, Integer> results = new Hashtable<>();

        results.put("bollinger", bollingerService.calculateAtDate(symbol, 20, 2.0, date, 0.1).getScore());
        results.put("dmi", dmiService.calculateDMI(symbol, 14, date).getScore());
        results.put("ma-cross", macrossService.calculateEMACross(symbol, 50, 100).getScore());
        results.put("macd", macdService.calculateMacd(symbol, 12, 26, 9, date, 7, 0.005).getScore());
        results.put("mfi", mfiService.calculateMFI(symbol, 14, date, 20, 80).getScore());
        results.put("rsi", rsiService.calculateRSI(symbol, 14, date, 5, 30, 70, 50).getScore());
        results.put("trendline", trendlineService.calculateTrendline(symbol, 20, 50, date).getScore());

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

}