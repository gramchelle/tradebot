package lion.mode.tradebot_backend.service.fetchdata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lion.mode.tradebot_backend.model.StockDataDaily;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlphaVantageDataCollectorService {
    private final StockDataRepository repository;
    private final HttpClient httpClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    public boolean saveStockData(String symbol) {
        try {
            String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY"
                    + "&symbol=" + symbol
                    + "&apikey=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject timeSeries = json.getAsJsonObject("Time Series (Daily)");

            if (timeSeries == null) {
                System.err.println("[!] Not able to fetch data for " + symbol + ". API Answer: " + response.body());
                return false;
            }

            List<StockDataDaily> newStockList = new ArrayList<>();
            LocalDateTime lastTimestampInDb = repository.findTopBySymbolOrderByTimestampDesc(symbol)
                    .map(StockDataDaily::getTimestamp)
                    .orElse(null);

            for (Map.Entry<String, JsonElement> entry : timeSeries.entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey(), FORMATTER);
                LocalDateTime utcTime = date.atStartOfDay(ZoneOffset.UTC).toLocalDateTime();

                if (lastTimestampInDb == null || utcTime.isAfter(lastTimestampInDb)) {
                    JsonObject values = entry.getValue().getAsJsonObject();
                    StockDataDaily data = new StockDataDaily();
                    data.setSymbol(symbol);
                    data.setOpen(values.get("1. open").getAsDouble());
                    data.setHigh(values.get("2. high").getAsDouble());
                    data.setLow(values.get("3. low").getAsDouble());
                    data.setClose(values.get("4. close").getAsDouble());
                    data.setVolume(values.get("5. volume").getAsLong());
                    data.setTimestamp(utcTime);
                    newStockList.add(data);
                }
            }

            if (!newStockList.isEmpty()) {
                Collections.reverse(newStockList);
                repository.saveAll(newStockList);
                System.out.println("[i] Saved " + newStockList.size() + " new bars for " + symbol);
                return true;
            }
            return false;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("[!] Alpha Vantage Error on Fetching Data: " + e.getMessage());
        }
    }
}