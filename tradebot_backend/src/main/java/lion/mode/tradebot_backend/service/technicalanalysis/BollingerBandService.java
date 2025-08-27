package lion.mode.tradebot_backend.service.technicalanalysis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;

import lion.mode.tradebot_backend.dto.indicators.BollingerResult;
import lion.mode.tradebot_backend.repository.StockDataRepository;

@Service
public class BollingerBandService extends IndicatorService {

    public BollingerBandService(StockDataRepository repository) {
        super(repository);
    }

    public BollingerResult calculateBollingerBands(String symbol, int period) {
        BarSeries series = loadSeries(symbol);
        int lastIndex = series.getEndIndex();

        BollingerResult result = new BollingerResult();
        result.setSymbol(symbol);

        result.setPeriod(period);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator sma = new SMAIndicator(closePrice, period);
        BollingerBandsMiddleIndicator middleBand = new BollingerBandsMiddleIndicator(sma);

        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, period);

        BollingerBandsUpperIndicator upperBand = new BollingerBandsUpperIndicator(middleBand, sd);
        BollingerBandsLowerIndicator lowerBand = new BollingerBandsLowerIndicator(middleBand, sd);

        double middle = middleBand.getValue(lastIndex).doubleValue();
        double upper = upperBand.getValue(lastIndex).doubleValue();
        double lower = lowerBand.getValue(lastIndex).doubleValue();
        double close = closePrice.getValue(lastIndex).doubleValue();

        result.setMiddleBand(middle);
        result.setUpperBand(upper);
        result.setLowerBand(lower);
        result.setClosePrice(close);

        if (close >= upper) {
            result.setSignal("sell");
            result.setComment("Price is close to the upper band (possible overbought).");
        } else if (close <= lower) {
            result.setSignal("buy");
            result.setComment("Price is close to the lower band (possible oversold).");
        } else {
            result.setSignal("hold");
            result.setComment("Price is between bands (neutral).");
        }

        return result;
    }

    public List<BollingerResult> calculateBollingerBandsForRange(String symbol, int period, LocalDate startDate, LocalDate endDate) {
        BarSeries series = loadSeries(symbol);
        List<BollingerResult> results = new ArrayList<>();

        for (int i = period - 1; i < series.getEndIndex(); i++) {
            BollingerResult result = new BollingerResult();
            result.setSymbol(symbol);
            result.setPeriod(period);

            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            SMAIndicator sma = new SMAIndicator(closePrice, period);
            BollingerBandsMiddleIndicator middleBand = new BollingerBandsMiddleIndicator(sma);
            StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, period);
            BollingerBandsUpperIndicator upperBand = new BollingerBandsUpperIndicator(middleBand, sd);
            BollingerBandsLowerIndicator lowerBand = new BollingerBandsLowerIndicator(middleBand, sd);

            double middle = middleBand.getValue(i).doubleValue();
            double upper = upperBand.getValue(i).doubleValue();
            double lower = lowerBand.getValue(i).doubleValue();
            double close = closePrice.getValue(i).doubleValue();
            
            // TODO: handle volatility 
            
            result.setMiddleBand(middle);
            result.setUpperBand(upper);
            result.setLowerBand(lower);
            result.setClosePrice(close);

            if (close >= upper) {
                result.setSignal("sell");
                result.setComment("Price is close to the upper band (possible overbought).");
            } else if (close <= lower) {
                result.setSignal("buy");
                result.setComment("Price is close to the lower band (possible oversold).");
            } else {
                result.setSignal("hold");
                result.setComment("Price is between bands (neutral).");
            }

            results.add(result);
        }

        return results;
    }
}