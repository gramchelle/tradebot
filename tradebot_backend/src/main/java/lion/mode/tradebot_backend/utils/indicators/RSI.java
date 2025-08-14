package lion.mode.tradebot_backend.utils.indicators;

import java.util.List;

public class RSI {

    public static double calculateRSI(List<Double> closes){
        int period = 14; // mostly 14 and 26
        double gain = 0;
        double loss = 0;

        for (int i = 1; i < closes.size(); i++) {
            double diff = closes.get(i) - closes.get(i - 1);
            if (diff >= 0) gain += diff;
            else loss -= diff; // make negative difference positive
        }

        double avgGain = gain / period;
        double avgLoss = loss / period;

        if (avgLoss == 0) return 100;

        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    public static double rsiScore(double rsi) {
        if (rsi <= 30) return 1.0;
        if (rsi >= 70) return -1.0;
        if (rsi > 30 && rsi < 50) return (50 - rsi) / 20;
        if (rsi > 50 && rsi < 70) return - (rsi - 50) / 20;
        return 0.0;
    }

}
