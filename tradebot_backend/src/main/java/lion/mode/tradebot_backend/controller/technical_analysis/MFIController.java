package lion.mode.tradebot_backend.controller.technical_analysis;

import lion.mode.tradebot_backend.dto.indicators.MFIResult;
import lion.mode.tradebot_backend.service.technicalanalysis.MFIService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/mfi")
public class MFIController {

    private final MFIService mfiService;

    public MFIController(MFIService mfiService) {
        this.mfiService = mfiService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<MFIResult> getLatestMFI(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period) {
        return new ResponseEntity<>(mfiService.calculateMFI(symbol, period), HttpStatus.OK);
    }

    @GetMapping("/{symbol}/at")
    public ResponseEntity<MFIResult> getMFIAtDate(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDate,
            @RequestParam(defaultValue = "14") int period) {
        return new ResponseEntity<>(mfiService.calculateMFIAt(symbol, period, targetDate), HttpStatus.OK);
    }

}
