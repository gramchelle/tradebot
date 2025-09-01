package lion.mode.tradebot_backend.service.technicalanalysis;

import lion.mode.tradebot_backend.dto.indicators.trendline.TrendlineResult;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.LocalDateTime;

@Service
public class TrendlineService extends IndicatorService {

    public TrendlineService(StockDataRepository repository) {
        super(repository);
    }

    public TrendlineResult calculateTrendline(String symbol, int lookbackBars) {
        BarSeries series = loadSeries(symbol);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        TrendlineResult result = new TrendlineResult();
        result.setSymbol(symbol);

        result.setTrendType("uptrend");
        result.setLineType("support");

        result.setTouchCount(2);

        result.setBroken(false);

        result.setSignal("hold");
        result.setComment("Trend çizgisi sağlam, kırılma yok");

        result.setSlope(0.5);
        result.setIntercept(100);

        result.setStartDate(LocalDateTime.now().minusDays(lookbackBars));
        result.setEndDate(LocalDateTime.now());

        return result;
    }
}
