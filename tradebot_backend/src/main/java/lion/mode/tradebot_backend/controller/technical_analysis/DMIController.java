package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.dto.indicators.DMIResult;
import lion.mode.tradebot_backend.service.technicalanalysis.DMIService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/dmi")
public class DMIController {

    private final DMIService dmiService;

    public DMIController(DMIService dmiService) {
        this.dmiService = dmiService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<DMIResult> getLatestDMI(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period) {
        return new ResponseEntity<>(dmiService.calculateDMI(symbol, period), HttpStatus.OK);
    }

    @GetMapping("/{symbol}/at")
    public ResponseEntity<DMIResult> getDMIAtDate(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDate,
            @RequestParam(defaultValue = "14") int period) {
        return new ResponseEntity<>(dmiService.calculateDMIAt(symbol, period, targetDate), HttpStatus.OK);
    }
}
