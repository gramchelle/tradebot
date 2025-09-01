package lion.mode.tradebot_backend.controller.technical_analysis_controllers;

import lion.mode.tradebot_backend.dto.indicators.bollinger_bands.BollingerResult;
import lion.mode.tradebot_backend.dto.indicators.bollinger_bands.BollingerSeriesEntry;
import lion.mode.tradebot_backend.dto.indicators.macd.MACrossResult;
import lion.mode.tradebot_backend.dto.indicators.macd.MacdResult;
import lion.mode.tradebot_backend.dto.indicators.macd.MacdSeriesEntry;
import lion.mode.tradebot_backend.dto.indicators.rsi.RSIResult;
import lion.mode.tradebot_backend.dto.indicators.trendline.TrendlineResult;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.service.fetch_data_api.AlphaVantageDataCollectorService;
import lion.mode.tradebot_backend.service.technicalanalysis.BollingerBandService;
import lion.mode.tradebot_backend.service.technicalanalysis.BollingerService;
import lion.mode.tradebot_backend.service.technicalanalysis.MACrossService;
import lion.mode.tradebot_backend.service.technicalanalysis.MacdService;
import lion.mode.tradebot_backend.service.technicalanalysis.RsiService;
import lion.mode.tradebot_backend.service.technicalanalysis.TrendlineService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        results.put("rsi", rsiService.calculateRSI(symbol, 14).getSignal());
        results.put("macd", macdService.calculateMacd(symbol, 12, 26, 9).getSignalText());
        results.put("maCross", macrossService.calculateMACross(symbol, 9, 26).getSignal());
        //results.put("bollingerMiddle", bollingerService.calculateBollinger(symbol, 20).getMiddle());
        results.put("trendSlope", trendlineService.calculateTrendline(symbol, 14).getTrendType());

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

}