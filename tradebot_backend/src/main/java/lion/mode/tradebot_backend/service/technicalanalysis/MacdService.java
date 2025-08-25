package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.indicators.MacdResult;
import lion.mode.tradebot_backend.exception.NotEnoughDataException;
import lion.mode.tradebot_backend.repository.StockDataRepository;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
public class MacdService extends IndicatorService {

    public MacdService(StockDataRepository stockDataRepository) {
        super(stockDataRepository);
    }

    public MacdResult calculateMacd(String symbol, int shortPeriod, int longPeriod, int signalPeriod) {
        BarSeries series = loadSeries(symbol);
        if (series.getBarCount() < longPeriod) {
            throw new NotEnoughDataException("Not enough data to calculate MACD for " + symbol);
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator shortEma = new EMAIndicator(closePrice, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(closePrice, longPeriod);

        MACDIndicator macd = new MACDIndicator(closePrice, shortPeriod, longPeriod);
        EMAIndicator signal = new EMAIndicator(macd, signalPeriod);

        int endIndex = series.getEndIndex();

        MacdResult result = new MacdResult();
        result.setSymbol(symbol);
        result.setShortPeriodEMA(shortEma.getValue(endIndex).doubleValue());
        result.setLongPeriodEMA(longEma.getValue(endIndex).doubleValue());
        result.setMacdScore(macd.getValue(endIndex).doubleValue());
        result.setSignalScore(signal.getValue(endIndex).doubleValue());
        result.setHistogramValue(result.getMacdScore() - result.getSignalScore());

        if (result.getHistogramValue() > 0) {
            result.setTradeSignal("buy");
        } else {
            result.setTradeSignal("sell");
        }

        String divergence = detectDivergence(series, longPeriod, symbol);
        result.setDivergence(divergence);

        return result;
    }

    public MacdResult calculateMacd(String symbol, int shortPeriod, int longPeriod, int signalPeriod, LocalDate startDate, LocalDate endDate) {
        BarSeries series = loadSeries(symbol);
        if (series.getBarCount() < longPeriod) {
            throw new NotEnoughDataException("Not enough data to calculate MACD for " + symbol);
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator shortEma = new EMAIndicator(closePrice, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(closePrice, longPeriod);

        MACDIndicator macd = new MACDIndicator(closePrice, shortPeriod, longPeriod);
        EMAIndicator signal = new EMAIndicator(macd, signalPeriod);

        int endIndex = series.getEndIndex();

        MacdResult result = new MacdResult();
        result.setSymbol(symbol);
        result.setShortPeriodEMA(shortEma.getValue(endIndex).doubleValue());
        result.setLongPeriodEMA(longEma.getValue(endIndex).doubleValue());
        result.setMacdScore(macd.getValue(endIndex).doubleValue());
        result.setSignalScore(signal.getValue(endIndex).doubleValue());
        result.setHistogramValue(result.getMacdScore() - result.getSignalScore());

        if (result.getHistogramValue() > 0) {
            result.setTradeSignal("buy");
        } else {
            result.setTradeSignal("sell");
        }

        String divergence = detectDivergence(series, 90, symbol);
        result.setDivergence(divergence);

        return result;
    }

}
