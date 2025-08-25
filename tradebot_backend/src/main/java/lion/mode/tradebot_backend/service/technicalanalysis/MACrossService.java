package lion.mode.tradebot_backend.service.technicalanalysis;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import lion.mode.tradebot_backend.dto.indicators.MACrossResult;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;

@Service
public class MACrossService extends IndicatorService {

    public MACrossService(StockDataRepository repository) {
        super(repository);
    }

    // TODO: write calculateCrossesInRange function

    public MACrossResult calculateMACross(String symbol, int shortPeriod, int longPeriod) {
        BarSeries series = loadSeries(symbol);
        MACrossResult result = new MACrossResult();

        if (series.getBarCount() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MA Cross for " + symbol);
        }

        EMAIndicator shortEma = new EMAIndicator(new ClosePriceIndicator(series), shortPeriod);
        EMAIndicator longEma = new EMAIndicator(new ClosePriceIndicator(series), longPeriod);
        MACDIndicator macd = new MACDIndicator(new ClosePriceIndicator(series), shortPeriod, longPeriod);

        result.setSymbol(symbol);
        result.setShortMAValue(shortEma.getValue(series.getEndIndex()).doubleValue());
        result.setLongMAValue(longEma.getValue(series.getEndIndex()).doubleValue());
        result.setCrossoverPoint(macd.getValue(series.getEndIndex()).doubleValue());
        result.setSignal(macd.getValue(series.getEndIndex()).doubleValue() > 0 ? "BUY" : "SELL");

        return result;
   
    }

}
