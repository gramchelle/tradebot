package lion.mode.tradebot_backend.dto.indicators.rsi;

import java.time.LocalDateTime;

public class RsiSeriesEntry {
    private LocalDateTime timestamp;
    private double rsi;

    public RsiSeriesEntry() {}

    public RsiSeriesEntry(LocalDateTime timestamp, double rsi) {
        this.timestamp = timestamp;
        this.rsi = rsi;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public double getRsi() { return rsi; }
    public void setRsi(double rsi) { this.rsi = rsi; }
}