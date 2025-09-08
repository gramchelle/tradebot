package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.indicators.BollingerResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;

import java.time.*;

@Service
public class BollingerBandsService extends IndicatorService {

    public BollingerBandsService(StockDataRepository repository) {
        super(repository);
    }

    public BollingerResult calculateAtDate(String symbol, int period, double nbDev, LocalDateTime date, double squeezeConfidence) {
        BarSeries series = loadSeries(symbol);

        if (series.getBarCount() < period) {
            throw new NotEnoughDataException("Not enough data for Bollinger Bands for symbol: " + symbol);
        }

        int targetIndex = seriesAmountValidator(symbol, series, date);

        return calculateAtIndex(symbol, series, period, nbDev, targetIndex, squeezeConfidence);
    }

    private BollingerResult calculateAtIndex(String symbol, BarSeries series, int period, double nbDev, int index, double squeezeConfidence) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, period);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, period);

        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd, DecimalNum.valueOf(nbDev));
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd, DecimalNum.valueOf(nbDev));

        double middle = bbm.getValue(index).doubleValue();
        double upper = bbu.getValue(index).doubleValue();
        double lower = bbl.getValue(index).doubleValue();
        double bandwidth = (upper - lower) / middle;

        double close = closePrice.getValue(index).doubleValue();

        BollingerResult result = new BollingerResult();
        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setNumberOfDeviations(nbDev);
        result.setMiddle(middle);
        result.setUpper(upper);
        result.setLower(lower);
        result.setBandwidth(bandwidth);

        // Squeeze detection
        boolean squeeze = bandwidth < squeezeConfidence;
        result.setSqueeze(squeeze ? "squeeze detected" : "squeeze not detected");

        if (close >= upper) {
            result.setSignal("Sell");
            result.setScore(-1);
        } else if (close <= lower) {
            result.setSignal("Buy");
            result.setScore(1);
        } else {
            result.setSignal("Hold");
            result.setScore(0);
        }

        return result;
    }
}
