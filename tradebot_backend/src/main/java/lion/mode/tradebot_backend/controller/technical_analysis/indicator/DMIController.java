package lion.mode.tradebot_backend.controller.technical_analysis.indicator;

import lion.mode.tradebot_backend.dto.indicator.DMIResult;
import lion.mode.tradebot_backend.service.technicalanalysis.indicator.DMIService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ta/dmi")
@RequiredArgsConstructor
public class DMIController {

    private final DMIService dmiService;

    @GetMapping("/{symbol}")
    public ResponseEntity<DMIResult> getDMIAtDate(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "#{T(java.time.LocalDateTime).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "14") int period
    ) {
        return new ResponseEntity<>(dmiService.calculateDMI(symbol, period, date), HttpStatus.OK);
    }
}
