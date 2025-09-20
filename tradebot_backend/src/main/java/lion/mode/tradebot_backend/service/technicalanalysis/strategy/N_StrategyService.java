package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.N_StrategyBacktestDto;
import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ProfitCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

public class N_StrategyService extends IndicatorService{

    public N_StrategyService(StockDataRepository repository, BacktestRepository backtestRepository) {
        super(repository, backtestRepository);
    }

    public Strategy buildRsiStrategy(BarSeries series, String source, String indicatorName, Map<String, Object> params){
        if (series==null) throw new IllegalArgumentException("Series cannot be null");
        Indicator<Num> prices = sourceSelector(source, series);


        SMAIndicator shortSma = new SMAIndicator(prices, 5);
        SMAIndicator longSma = new SMAIndicator(prices, 10);

        RSIIndicator rsi = new RSIIndicator(prices, 14);

        Rule entryRule = new OverIndicatorRule(shortSma, longSma)
                .and(new CrossedDownIndicatorRule(rsi, 30))
                .and(new OverIndicatorRule(shortSma, prices));

        Rule exitRule = new UnderIndicatorRule(shortSma, longSma)
                .and(new CrossedUpIndicatorRule(rsi, 70))
                .and(new UnderIndicatorRule(shortSma, prices));

        return new BaseStrategy(entryRule, exitRule);
    }

    public Strategy buildStrategy(BarSeries series){
        if (series==null) throw new IllegalArgumentException("Series cannot be null");
        Indicator<Num> prices = sourceSelector("close", series);

        SMAIndicator shortSma = new SMAIndicator(prices, 5);
        SMAIndicator longSma = new SMAIndicator(prices, 10);

        RSIIndicator rsi = new RSIIndicator(prices, 14);

        Rule entryRule = new OverIndicatorRule(shortSma, longSma)
                .and(new CrossedDownIndicatorRule(rsi, 30))
                .and(new OverIndicatorRule(shortSma, prices));

        Rule exitRule = new UnderIndicatorRule(shortSma, longSma)
                .and(new CrossedUpIndicatorRule(rsi, 70))
                .and(new UnderIndicatorRule(shortSma, prices));

        return new BaseStrategy(entryRule, exitRule);
    }

    public void run(String symbol) {
        BarSeries series = loadSeries(symbol);

        Strategy strategy = buildStrategy(series);
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);
        System.out.println("Number of positions for the strategy: " + tradingRecord.getPositionCount());

        // Analysis
        System.out.println("Total return for the strategy: " + new ReturnCriterion().calculate(series, tradingRecord));
        System.out.println("Profit Loss Percentage: " + new ProfitLossPercentageCriterion().calculate(series, tradingRecord));

    }

}
