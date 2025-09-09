package lion.mode.tradebot_backend.dto.backtest;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BacktestResult {

    private int id;

    private String symbol;

    private String indicator;

    private List<String> strategy; // can be one indicator or multiple indicators

    private String signal;

    private int score;

    private LocalDateTime stockDate;

    private boolean isMatch;

    private LocalDateTime queriedAt;
}
