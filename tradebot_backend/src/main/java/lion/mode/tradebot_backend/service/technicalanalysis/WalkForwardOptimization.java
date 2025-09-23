package lion.mode.tradebot_backend.service.N_technicalanalysis;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.num.Num;

import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import ta4jexamples.strategies.CCICorrectionStrategy;
import ta4jexamples.strategies.GlobalExtremaStrategy;
import ta4jexamples.strategies.MovingMomentumStrategy;
import ta4jexamples.strategies.RSI2Strategy;
import lion.mode.tradebot_backend.dto.base_responses.N_BacktestReport;

@Service
@RequiredArgsConstructor
public class WalkForwardOptimization {

    private final StockDataRepository repository;
    private final N_StrategyService strategyService;
    
    public N_BacktestReport runCustomConfluenceStrategy(String symbol) {
        BarSeries series = strategyService.loadSeries(symbol);

        List<BarSeries> subseries = splitSeries(series, Duration.ofDays(2), Duration.ofDays(252));

        Map<Strategy, String> strategies = buildStrategiesMap(series);

        AnalysisCriterion returnCriterion = new ReturnCriterion();

        for (BarSeries slice : subseries) {
            System.out.println("Sub-series: " + slice.getSeriesPeriodDescription());
            BarSeriesManager sliceManager = new BarSeriesManager(slice);
            for (Map.Entry<Strategy, String> entry : strategies.entrySet()) {
                Strategy strategy = entry.getKey();
                String name = entry.getValue();
                TradingRecord tradingRecord = sliceManager.run(strategy);
                Num profit = returnCriterion.calculate(slice, tradingRecord);
                System.out.println("\tProfit for " + name + ": " + profit);
            }
            Strategy bestStrategy = returnCriterion.chooseBest(sliceManager, TradeType.BUY,
                    new ArrayList<Strategy>(strategies.keySet()));
            System.out.println("\t\t--> Best strategy: " + strategies.get(bestStrategy) + "\n");
        }

        return null;
    }

    /// Helpers - ta4j.example.WalkForward

    public static Map<Strategy, String> buildStrategiesMap(BarSeries series) {
        HashMap<Strategy, String> strategies = new HashMap<>();
        strategies.put(CCICorrectionStrategy.buildStrategy(series), "CCI Correction");
        strategies.put(GlobalExtremaStrategy.buildStrategy(series), "Global Extrema");
        strategies.put(MovingMomentumStrategy.buildStrategy(series), "Moving Momentum");
        strategies.put(RSI2Strategy.buildStrategy(series), "RSI-2");
        return strategies;
    }

    public static List<Integer> getSplitBeginIndexes(BarSeries series, Duration splitDuration) {
        ArrayList<Integer> beginIndexes = new ArrayList<>();

        int beginIndex = series.getBeginIndex();
        int endIndex = series.getEndIndex();

        beginIndexes.add(beginIndex);

        // Building the first interval before next split
        Instant beginInterval = series.getFirstBar().getEndTime();
        Instant endInterval = beginInterval.plus(splitDuration);

        for (int i = beginIndex; i <= endIndex; i++) {
            // For each bar...
            Instant barTime = series.getBar(i).getEndTime();
            if (barTime.isBefore(beginInterval) || !barTime.isBefore(endInterval)) {
                // Bar out of the interval
                if (!endInterval.isAfter(barTime)) {
                    // Bar after the interval
                    // --> Adding a new begin index
                    beginIndexes.add(i);
                }

                // Building the new interval before next split
                beginInterval = endInterval.isBefore(barTime) ? barTime : endInterval;
                endInterval = beginInterval.plus(splitDuration);
            }
        }
        return beginIndexes;
    }

    public static BarSeries subseries(BarSeries series, int beginIndex, Duration duration) {

        Instant beginInterval = series.getBar(beginIndex).getEndTime();
        Instant endInterval = beginInterval.plus(duration);

        // Checking bars belonging to the sub-series (starting at the provided index)
        int subseriesNbBars = 0;
        int endIndex = series.getEndIndex();
        for (int i = beginIndex; i <= endIndex; i++) {
            // For each bar...
            Instant barTime = series.getBar(i).getEndTime();
            if (barTime.isBefore(beginInterval) || !barTime.isBefore(endInterval)) {
                // Bar out of the interval
                break;
            }
            // Bar in the interval
            // --> Incrementing the number of bars in the subseries
            subseriesNbBars++;
        }

        return series.getSubSeries(beginIndex, beginIndex + subseriesNbBars);
    }

    public static List<BarSeries> splitSeries(BarSeries series, Duration splitDuration, Duration sliceDuration) {
        ArrayList<BarSeries> subseries = new ArrayList<>();
        if (splitDuration != null && !splitDuration.isZero() && sliceDuration != null && !sliceDuration.isZero()) {

            List<Integer> beginIndexes = getSplitBeginIndexes(series, splitDuration);
            for (Integer subseriesBegin : beginIndexes) {
                subseries.add(subseries(series, subseriesBegin, sliceDuration));
            }
        }
        return subseries;
    }

}
