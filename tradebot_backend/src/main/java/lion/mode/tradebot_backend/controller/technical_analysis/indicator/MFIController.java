package lion.mode.tradebot_backend.controller.technical_analysis.indicator;

import lion.mode.tradebot_backend.dto.indicator.MFIResult;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.MFIService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/mfi")
@RequiredArgsConstructor
public class MFIController {

    private final MFIService mfiService;

    @GetMapping("/{symbol}")
    public ResponseEntity<MFIResult> getMFIAtDate(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "14") int period,
            @RequestParam(defaultValue = "20") int lowerLimit,
            @RequestParam(defaultValue = "80") int upperLimit
    ) {
        return new ResponseEntity<>(mfiService.calculateMFI(symbol, period, date, lowerLimit, upperLimit), HttpStatus.OK);
    }

}
