package lion.mode.tradebot_backend.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BacktestTrials {

    private Long id;
    private Long backtestId;
    private int trialIndex;
    private LocalDateTime entryDate;
    private LocalDateTime exitDate;
    private double entryPrice;
    private double exitPrice;
    private int prediction;
    private boolean success;
    private double score;
    private int barsSinceSignal;
    private String indicatorValues;
    private double priceDiff;
}
