package lion.mode.tradebot_backend.service;

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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataCollectorService { // streaming data from alpha vantage

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    private final StockDataRepository repository;

    public List<StockData> fetchStockData() {
        try {
            String symbol = "AAPL";
            String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY" // Intraday can be changed
                    + "&symbol=" + symbol
                    + "&interval=1min&apikey=" + apiKey;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject timeSeries = json.getAsJsonObject("Time Series (1min)");

            // İlk veri noktasını al
            Map.Entry<String, com.google.gson.JsonElement> firstEntry =
                    timeSeries.entrySet().iterator().next();

            String timeStampStr = firstEntry.getKey();
            JsonObject latestData = firstEntry.getValue().getAsJsonObject();

            StockData data = new StockData();
            data.setSymbol(symbol);
            data.setOpen(latestData.get("1. open").getAsDouble());
            data.setHigh(latestData.get("2. high").getAsDouble());
            data.setLow(latestData.get("3. low").getAsDouble());
            data.setClose(latestData.get("4. close").getAsDouble());
            data.setVolume(latestData.get("5. volume").getAsLong());
            data.setTimestamp(LocalDateTime.parse(timeStampStr.replace(" ", "T")));

            repository.save(data);
            return repository.findAll();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Alpha Vantage veri çekme hatası: " + e.getMessage());
        }
    }


}