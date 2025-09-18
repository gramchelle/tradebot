package lion.mode.tradebot_backend.service.technicalanalysis.backtest;

import lion.mode.tradebot_backend.repository.BacktestRepository;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

import java.util.HashMap;
import java.util.List;

@Service
public class BacktestService extends AbstractBacktestService{


    public BacktestService(StockDataRepository repository, BacktestRepository  backtestRepository) {
        super(repository,  backtestRepository);
    }


    public HashMap<String, Double> calculateBollinger(String symbol){
        BarSeries series = loadSeries(symbol);
        BollingerBandFacade bollingerBandFacade = new BollingerBandFacade(series, 20, 2);

        Double upper = bollingerBandFacade.upper().stream().mapToDouble(Num::doubleValue).max().orElse(0.0);
        Double lower = bollingerBandFacade.lower().stream().mapToDouble(Num::doubleValue).max().orElse(0.0);
        Double middle = bollingerBandFacade.middle().stream().mapToDouble(Num::doubleValue).max().orElse(0.0);
        Double percentB = bollingerBandFacade.percentB().stream().mapToDouble(Num::doubleValue).max().orElse(0.0);
        Double bandwidth = bollingerBandFacade.bandwidth().stream().mapToDouble(Num::doubleValue).max().orElse(0.0);

        HashMap<String, Double> result = new HashMap<>();

        result.put("upperBand", upper);
        result.put("middleBand", middle);
        result.put("lowerBand", lower);
        result.put("bandwidth", bandwidth);
        result.put("percentB", percentB);

        return result;
    }



}
