package lion.mode.tradebot_backend.controller.technical_analysis_controllers;

import lion.mode.tradebot_backend.dto.indicators.macd.MACrossResult;
import lion.mode.tradebot_backend.service.technicalanalysis.MACrossService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ta/macross")
@RequiredArgsConstructor
public class MACrossController {

    private final MACrossService macrossService;

    @GetMapping("/ma-cross")
    public ResponseEntity<MACrossResult> getMACrossScore(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "9") int shortPeriod,
            @RequestParam(defaultValue = "26") int longPeriod) {

    MACrossResult macrossResult = macrossService.calculateMACross(symbol, shortPeriod, longPeriod);
    return new ResponseEntity<>(macrossResult, HttpStatus.OK);
    }

}
