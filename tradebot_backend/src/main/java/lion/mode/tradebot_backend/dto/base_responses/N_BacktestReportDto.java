package lion.mode.tradebot_backend.dto.base_responses;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class N_BacktestReportDto {

    private Long id;
    private String strategyName;
    private String symbol;
    private Instant date;
    private Long lookback;
    private Instant startDate;

    // -- performans --

    // başlangıç bakiyesi
    // son overall
    // komisyon masrafları
    // faiz gelirleri
    // getiri
    // yüzde getiri

    // -- işlemler --

    // 
    


}
