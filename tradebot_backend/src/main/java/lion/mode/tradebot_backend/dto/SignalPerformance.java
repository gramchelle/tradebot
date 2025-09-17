package lion.mode.tradebot_backend.dto;


import lombok.Data;

@Data
class SignalPerformance {

    private int totalSignals;
    private int correctPredictions;
    private int incorrectPredictions;
    private double accuracy;
    private double averagePriceMovement;
    private double bestPrediction;      // Best price movement achieved
    private double worstPrediction;     // Worst price movement

    public SignalPerformance() {}

    public SignalPerformance(int total, int correct) {
        this.totalSignals = total;
        this.correctPredictions = correct;
        this.incorrectPredictions = total - correct;
        this.accuracy = total > 0 ? (double) correct / total : 0.0;
    }

    public double getSuccessRate() {
        return totalSignals > 0 ? (double) correctPredictions / totalSignals : 0.0;
    }
}
