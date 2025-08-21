package lion.mode.tradebot_backend.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataCollectorService { // streaming data from alpha vantage

    private final StockDataRepository repository;
    private final HttpClient httpClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    public List<StockData> getAllStockData() {
        return repository.findAll();
    }

    public List<StockData> getStockDataBySymbol(String symbol) {
        return repository.findBySymbol(symbol);
    }

    // TODO: Belirli bir hisse senedi ve tarih aralığı için OHLCV verisini veritabanından çeken bir service katmanı yazılmalıdır.
    public List<StockData> getStockDataBySymbolAndDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findBySymbolAndTimestampBetween(symbol, startDate, endDate);
    }

    public boolean saveStockData(String symbol) {
        try {
            String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY"
                    + "&symbol=" + symbol
                    + "&interval=1min&apikey=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject timeSeries = json.getAsJsonObject("Time Series (1min)");

            if (timeSeries == null) {
                System.err.println("[!] " + symbol + " için 'Time Series (1min)' verisi alınamadı. API Cevabı: " + response.body());
                return false;
            }

            List<StockData> newStockDataList = new ArrayList<>();
            LocalDateTime lastTimestampInDb = repository.findTopBySymbolOrderByTimestampDesc(symbol)
                    .map(StockData::getTimestamp)
                    .orElse(null);

            for (Map.Entry<String, JsonElement> entry : timeSeries.entrySet()) {
                LocalDateTime entryTimestamp = LocalDateTime.parse(entry.getKey(), FORMATTER);
                if (lastTimestampInDb == null || entryTimestamp.isAfter(lastTimestampInDb)) {
                    JsonObject values = entry.getValue().getAsJsonObject();
                    StockData data = new StockData();
                    data.setSymbol(symbol);
                    data.setOpen(values.get("1. open").getAsDouble());
                    data.setHigh(values.get("2. high").getAsDouble());
                    data.setLow(values.get("3. low").getAsDouble());
                    data.setClose(values.get("4. close").getAsDouble());
                    data.setVolume(values.get("5. volume").getAsLong());
                    data.setTimestamp(entryTimestamp);
                    newStockDataList.add(data);
                }
            }
            if (!newStockDataList.isEmpty()) {
                Collections.reverse(newStockDataList);
                repository.saveAll(newStockDataList);
                return true;
            }
            return false;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("[!] Alpha Vantage veri çekme hatası: " + e.getMessage());
        }
    }

}