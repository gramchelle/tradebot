package lion.mode.tradebot_backend.service.technicalanalysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lion.mode.tradebot_backend.dto.indicators.macd.MacdResult;
import lion.mode.tradebot_backend.dto.indicators.macd.MacdSeriesEntry;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MacdService extends IndicatorService {

    private final StockDataRepository repository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${alpha.vantage.api.key}")
    private String alphaVantageKey;

    public MacdService(StockDataRepository repository) {
        super(repository);
        this.repository = repository;
    }

    // load series from DB (limit = number of most recent bars to load, 0 => all)
    private BarSeries loadSeries(String symbol, int limit) {
        Duration barDuration = Duration.ofHours(1); // adjust if needed
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

    public MacdResult calculateMacd(String symbol, int shortP, int longP, int signalP) {
        // load sufficient history (long + signal + smoothing margin)
        int total = Math.max(longP, shortP) + signalP + 100;
        BarSeries series = loadSeries(symbol, total);
        if (series.getBarCount() < longP) throw new IllegalArgumentException("Not enough data");

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, shortP, longP);
        EMAIndicator signal = new EMAIndicator(macd, signalP);

        int idx = series.getEndIndex();
        double macdVal = macd.getValue(idx).doubleValue();
        double signalVal = signal.getValue(idx).doubleValue();
        double hist = DecimalNum.valueOf(macdVal).minus(DecimalNum.valueOf(signalVal)).doubleValue();

        MacdResult r = new MacdResult();
        r.setSymbol(symbol);
        r.setShortPeriod(shortP);
        r.setLongPeriod(longP);
        r.setSignalPeriod(signalP);
        r.setMacd(macdVal);
        r.setSignal(signalVal);
        r.setHistogram(hist);
        String div = detectDivergence(series, macd, signal);
        r.setDivergence(div);

        // basic signal from histogram crossing
        if (hist > 0) { r.setSignalText("Buy"); r.setScore(1); }
        else if (hist < 0) { r.setSignalText("Sell"); r.setScore(-1); }
        else { r.setSignalText("Hold"); r.setScore(0); }
        return r;
    }

    public MacdResult calculateMacd(String symbol, int shortP, int longP, int signalP,
                                   LocalDateTime start, LocalDateTime end) {
        List<StockData> dataList = repository.findBySymbolAndTimestampBetweenOrderByTimestampAsc(symbol, start, end);
        // build series similar to loadSeries but from dataList
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
        if (series.getBarCount() < longP) throw new IllegalArgumentException("Not enough data in range");
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, shortP, longP);
        EMAIndicator signal = new EMAIndicator(macd, signalP);

        int idx = series.getEndIndex();
        double macdVal = macd.getValue(idx).doubleValue();
        double signalVal = signal.getValue(idx).doubleValue();
        double hist = DecimalNum.valueOf(macdVal).minus(DecimalNum.valueOf(signalVal)).doubleValue();

        MacdResult r = new MacdResult();
        r.setSymbol(symbol);
        r.setShortPeriod(shortP);
        r.setLongPeriod(longP);
        r.setSignalPeriod(signalP);
        r.setMacd(macdVal);
        r.setSignal(signalVal);
        r.setHistogram(hist);
        r.setDivergence(detectDivergence(series, macd, signal));
        if (hist > 0) { r.setSignalText("buy"); r.setScore(1); } else if (hist < 0) { r.setSignalText("sell"); r.setScore(-1); } else { r.setSignalText("hold"); r.setScore(0); }
        return r;
    }

    // per-bar MACD series (for comparing with external charts)
    public List<MacdSeriesEntry> getMacdSeries(String symbol, int shortP, int longP, int signalP, int lookback) {
        int needed = Math.max(longP, shortP) + signalP + Math.max(100, lookback);
        BarSeries series = loadSeries(symbol, needed);
        if (series.getBarCount() < longP) return Collections.emptyList();
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(close, shortP, longP);
        EMAIndicator signal = new EMAIndicator(macd, signalP);
        List<MacdSeriesEntry> out = new ArrayList<>();
        int start = Math.max(longP, 0);
        for (int i = start; i <= series.getEndIndex(); i++) {
            double m = macd.getValue(i).doubleValue();
            double s = signal.getValue(i).doubleValue();
            double h = DecimalNum.valueOf(m).minus(DecimalNum.valueOf(s)).doubleValue();
            LocalDateTime ts = series.getBar(i).getEndTime().toLocalDateTime();
            out.add(new MacdSeriesEntry(ts, m, s, h));
        }
        return out;
    }

    // simple divergence detection (regular bullish/bearish) comparing price extrema vs MACD histogram
    public String detectDivergence(BarSeries series, MACDIndicator macd, EMAIndicator signal) {
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        int end = series.getEndIndex();
        int lookback = Math.min(series.getBarCount() - 1, 30);
        int start = Math.max(1, end - lookback);
        List<Integer> lows = new ArrayList<>();
        List<Integer> highs = new ArrayList<>();
        for (int i = start; i <= end - 1; i++) {
            double p = close.getValue(i).doubleValue();
            double prev = close.getValue(i - 1).doubleValue();
            double next = close.getValue(i + 1).doubleValue();
            if (p < prev && p < next) lows.add(i);
            if (p > prev && p > next) highs.add(i);
        }
        if (lows.size() >= 2) {
            int i1 = lows.get(lows.size() - 2), i2 = lows.get(lows.size() - 1);
            double p1 = close.getValue(i1).doubleValue(), p2 = close.getValue(i2).doubleValue();
            double hist1 = DecimalNum.valueOf(macd.getValue(i1).doubleValue()).minus(DecimalNum.valueOf(signal.getValue(i1).doubleValue())).doubleValue();
            double hist2 = DecimalNum.valueOf(macd.getValue(i2).doubleValue()).minus(DecimalNum.valueOf(signal.getValue(i2).doubleValue())).doubleValue();
            if (p2 < p1 && hist2 > hist1) return "bullish";
        }
        if (highs.size() >= 2) {
            int i1 = highs.get(highs.size() - 2), i2 = highs.get(highs.size() - 1);
            double p1 = close.getValue(i1).doubleValue(), p2 = close.getValue(i2).doubleValue();
            double hist1 = DecimalNum.valueOf(macd.getValue(i1).doubleValue()).minus(DecimalNum.valueOf(signal.getValue(i1).doubleValue())).doubleValue();
            double hist2 = DecimalNum.valueOf(macd.getValue(i2).doubleValue()).minus(DecimalNum.valueOf(signal.getValue(i2).doubleValue())).doubleValue();
            if (p2 > p1 && hist2 < hist1) return "bearish";
        }
        return "none";
    }

    // Optional: fetch MACD from Alpha Vantage (returns JSON as string -> parse if needed)
    public JsonNode fetchMacdFromAlphaVantage(String symbol, String interval, int fast, int slow, int signalP) throws IOException, InterruptedException {
        if (alphaVantageKey == null || alphaVantageKey.isEmpty()) throw new IllegalStateException("AlphaVantage API key not set (alphavantage.apikey)");
        String url = String.format("https://www.alphavantage.co/query?function=MACD&symbol=%s&interval=%s&series_type=close&fastperiod=%d&slowperiod=%d&signalperiod=%d&apikey=%s",
                symbol, interval, fast, slow, signalP, alphaVantageKey);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(resp.body());
    }
}
