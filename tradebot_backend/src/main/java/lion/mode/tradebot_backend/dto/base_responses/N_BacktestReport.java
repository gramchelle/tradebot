package lion.mode.tradebot_backend.dto.base_responses;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class N_BacktestReport {

    //Detailed report of each backtest run MATRIKS PRIME - NOT IMPLEMENTED YET

    private Long id;
    private String strategyName;
    private String symbol;
    private Instant date;
    private int lookbackBars;
    private Instant startDate;

    private String mostProfitablePosition;
    private String mostLosingPosition;
    private String longestPosition;
    private String shortestPosition;

    // işlemsiz süre

    private String longestTimeNoTrade;
    private String longestTimeNoTradeBars;

    // son durum
    private String lastSignal;
    private Instant lastSignalDate;
    private double lastPrice;

}
