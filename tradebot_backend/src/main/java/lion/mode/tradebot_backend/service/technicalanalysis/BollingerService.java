package lion.mode.tradebot_backend.service.technicalanalysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lion.mode.tradebot_backend.*;
import lion.mode.tradebot_backend.dto.indicators.bollinger_bands.BollingerResult;
import lion.mode.tradebot_backend.dto.indicators.bollinger_bands.BollingerSeriesEntry;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BollingerService extends IndicatorService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${alphavantage.apikey:}")
    private String alphaVantageKey;

    public BollingerService(StockDataRepository repository) {
        super(repository);
    }

    // helper: load series (limit = most recent bars to load, 0 -> all)
    private BarSeries loadSeries(String symbol, int limit) {
        Duration barDuration = Duration.ofHours(1); // adjust to your stored timeframe
        List<StockData> all = repository.findBySymbolOrderByTimestampAsc(symbol);
        if (all == null || all.isEmpty()) return new BaseBarSeries(symbol, DecimalNum::valueOf);

        List<StockData> slice = (limit > 0 && all.size() > limit) ? all.subList(all.size() - limit, all.size()) : all;

        boolean timestampsAreBarStart = false;
        if (slice.size() > 1) {
            LocalDateTime t0 = slice.get(0).getTimestamp();
            LocalDateTime t1 = slice.get(1).getTimestamp();
            if (Duration.between(t0, t1).equals(barDuration)) timestampsAreBarStart = true;
        }

        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);
        for (StockData d : slice) {
            ZonedDateTime ts = d.getTimestamp().atZone(ZoneId.systemDefault());
            ZonedDateTime endTime = timestampsAreBarStart ? ts.plus(barDuration) : ts;
            Bar bar = BaseBar.builder()
                    .timePeriod(barDuration)
                    .endTime(endTime)
                    .openPrice(DecimalNum.valueOf(d.getOpen()))
                    .highPrice(DecimalNum.valueOf(d.getHigh()))
                    .lowPrice(DecimalNum.valueOf(d.getLow()))
                    .closePrice(DecimalNum.valueOf(d.getClose()))
                    .volume(DecimalNum.valueOf(d.getVolume()))
                    .build();
            if (series.getBarCount() == 0 || bar.getEndTime().isAfter(series.getLastBar().getEndTime())) {
                series.addBar(bar);
            }
        }
        return series;
    }

    public BollingerResult calculateBollinger(String symbol, int period, double nbDev) {
        int needed = period + 120; // margin for stable statistics
        BarSeries series = loadSeries(symbol, needed);
        if (series.getBarCount() < period) throw new IllegalArgumentException("Not enough data for Bollinger");

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(close, period);
        StandardDeviationIndicator std = new StandardDeviationIndicator(close, period);

        int idx = series.getEndIndex();
        double middle = sma.getValue(idx).doubleValue();
        double sd = std.getValue(idx).doubleValue();
        double upper = middle + nbDev * sd;
        double lower = middle - nbDev * sd;
        double bandwidth = (upper - lower) / (middle == 0 ? 1 : middle);

        double lastClose = close.getValue(idx).doubleValue();
        BollingerResult r = new BollingerResult();
        r.setSymbol(symbol);
        r.setPeriod(period);
        r.setNbDev(nbDev);
        r.setMiddle(middle);
        r.setUpper(upper);
        r.setLower(lower);
        r.setBandwidth(bandwidth);
        r.setSqueeze(detectSqueeze(series, period, nbDev) ? "squeeze" : "none");

        // breakout signal
        if (lastClose > upper) { r.setSignal("BearishBreakout"); r.setScore(-1); }
        else if (lastClose < lower) { r.setSignal("BullishBreakout"); r.setScore(1); }
        else { r.setSignal("Neutral"); r.setScore(0); }
        return r;
    }

    public BollingerResult calculateBollinger(String symbol, int period, double nbDev, LocalDateTime start, LocalDateTime end) {
        List<StockData> dataList = repository.findBySymbolAndTimestampBetweenOrderByTimestampAsc(symbol, start, end);
        Duration barDuration = Duration.ofHours(1);
        BarSeries series = new BaseBarSeries(symbol, DecimalNum::valueOf);

        boolean timestampsAreBarStart = false;
        if (dataList.size() > 1) {
            if (Duration.between(dataList.get(0).getTimestamp(), dataList.get(1).getTimestamp()).equals(barDuration))
                timestampsAreBarStart = true;
        }
        for (StockData d : dataList) {
            ZonedDateTime ts = d.getTimestamp().atZone(ZoneId.systemDefault());
            ZonedDateTime endTime = timestampsAreBarStart ? ts.plus(barDuration) : ts;
            Bar bar = BaseBar.builder()
                    .timePeriod(barDuration)
                    .endTime(endTime)
                    .openPrice(DecimalNum.valueOf(d.getOpen()))
                    .highPrice(DecimalNum.valueOf(d.getHigh()))
                    .lowPrice(DecimalNum.valueOf(d.getLow()))
                    .closePrice(DecimalNum.valueOf(d.getClose()))
                    .volume(DecimalNum.valueOf(d.getVolume()))
                    .build();
            if (series.getBarCount() == 0 || bar.getEndTime().isAfter(series.getLastBar().getEndTime())) series.addBar(bar);
        }

        if (series.getBarCount() < period) throw new IllegalArgumentException("Not enough data in range for Bollinger");

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(close, period);
        StandardDeviationIndicator std = new StandardDeviationIndicator(close, period);
        int idx = series.getEndIndex();
        double middle = sma.getValue(idx).doubleValue();
        double sd = std.getValue(idx).doubleValue();
        double upper = middle + nbDev * sd;
        double lower = middle - nbDev * sd;
        double bandwidth = (upper - lower) / (middle == 0 ? 1 : middle);

        double lastClose = close.getValue(idx).doubleValue();
        BollingerResult r = new BollingerResult();
        r.setSymbol(symbol);
        r.setPeriod(period);
        r.setNbDev(nbDev);
        r.setMiddle(middle);
        r.setUpper(upper);
        r.setLower(lower);
        r.setBandwidth(bandwidth);
        r.setSqueeze(detectSqueeze(series, period, nbDev) ? "squeeze" : "none");

        if (lastClose > upper) { r.setSignal("BearishBreakout"); r.setScore(-1); }
        else if (lastClose < lower) { r.setSignal("BullishBreakout"); r.setScore(1); }
        else { r.setSignal("Neutral"); r.setScore(0); }
        return r;
    }

    public List<BollingerSeriesEntry> getBollingerSeries(String symbol, int period, double nbDev, int lookback) {
        int needed = period + Math.max(200, lookback);
        BarSeries series = loadSeries(symbol, needed);
        if (series.getBarCount() < period) return Collections.emptyList();

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(close, period);
        StandardDeviationIndicator std = new StandardDeviationIndicator(close, period);

        List<BollingerSeriesEntry> out = new ArrayList<>();
        int start = period;
        for (int i = start; i <= series.getEndIndex(); i++) {
            double c = close.getValue(i).doubleValue();
            double m = sma.getValue(i).doubleValue();
            double s = std.getValue(i).doubleValue();
            double upper = m + nbDev * s;
            double lower = m - nbDev * s;
            double bandwidth = (upper - lower) / (m == 0 ? 1 : m);
            out.add(new BollingerSeriesEntry(series.getBar(i).getEndTime().toLocalDateTime(), c, m, upper, lower, bandwidth));
        }
        return out;
    }

    // detect squeeze: compare current bandwidth to average of recent bandwidths
    public boolean detectSqueeze(BarSeries series, int period, double nbDev) {
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(close, period);
        StandardDeviationIndicator std = new StandardDeviationIndicator(close, period);
        int end = series.getEndIndex();
        int look = Math.min(20, series.getBarCount() - period);
        if (look <= 0) return false;
        List<Double> widths = new ArrayList<>();
        for (int i = Math.max(period, end - look + 1); i <= end; i++) {
            double m = sma.getValue(i).doubleValue();
            double s = std.getValue(i).doubleValue();
            double upper = m + nbDev * s;
            double lower = m - nbDev * s;
            widths.add((upper - lower) / (m == 0 ? 1 : m));
        }
        double sum = 0;
        for (double w : widths) sum += w;
        double avg = sum / widths.size();
        double cur = widths.get(widths.size() - 1);
        // squeeze if current bandwidth < 0.5 * average (tunable)
        return cur < (0.5 * avg);
    }

    // fetch BBANDS from Alpha Vantage (optional)
    public JsonNode fetchBbandsFromAlphaVantage(String symbol, String interval, int period, double nbdevUp, double nbdevDn) throws IOException, InterruptedException {
        if (alphaVantageKey == null || alphaVantageKey.isEmpty()) throw new IllegalStateException("AlphaVantage API key not set (alphavantage.apikey)");
        String url = String.format("https://www.alphavantage.co/query?function=BBANDS&symbol=%s&interval=%s&time_period=%d&series_type=close&nbdevup=%s&nbdevdn=%s&apikey=%s",
                symbol, interval, period, Double.toString(nbdevUp), Double.toString(nbdevDn), alphaVantageKey);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(resp.body());
    }
}