package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.service.technicalanalysis.indicator.BollingerBandsService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.DMIService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.MACrossService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.MFIService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.MACDService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.RSIService;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.TrendlineService;
import lombok.RequiredArgsConstructor;

import java.util.Dictionary;
import java.util.Hashtable;
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

    private String priceType = "close";
/*
    @GetMapping("/getSignals/{symbol}")
    public ResponseEntity<Dictionary<String, Integer>> getAllTechnicalIndicators(
        @PathVariable String symbol,
        @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
        ) {

        Dictionary<String, Integer> results = new Hashtable<>();

        results.put("bollinger", bollingerService.calculateAtDate(symbol, 20, 2.0, date, 0.1).getScore());
        results.put("dmi", dmiService.calculateDMI(symbol, 14, date).getScore());
        results.put("ma-cross", macrossService.calculateEMACross(symbol, 50, 200, date, 14, priceType).getScore());
        results.put("macd", macdService.calculateMacd(symbol, 12, 26, 9, date, 7, 0.005).getScore());
        results.put("mfi", mfiService.calculateMFI(symbol, 14, date, 25, 75).getScore());
        results.put("rsi", rsiService.calculateRSI(symbol, 14, date, 5, 35, 65, 50, priceType).getScore());
        results.put("trendline", trendlineService.calculateTrendline(symbol, 20, 50, date).getScore());

        return new ResponseEntity<>(results, HttpStatus.OK);
    }
*/
}