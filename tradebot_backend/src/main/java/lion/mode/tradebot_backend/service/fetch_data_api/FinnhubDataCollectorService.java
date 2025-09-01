package lion.mode.tradebot_backend.service.fetch_data_api;

import com.google.gson.JsonArray;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinnhubDataCollectorService {

    private final StockDataRepository repository;
    private final HttpClient httpClient;

    @Value("${finnhub.api.key}")
    private String apiKey;

    private static final ZoneId NASDAQ_ZONE = ZoneId.of("America/New_York");

    // Null güvenli getter metodları
    private String fieldSafe(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private double fieldSafeDouble(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsDouble() : 0.0;
    }

    private long fieldSafeLong(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsLong() : 0L;
    }

    // Tek sembol için son 2 haftalık 30dk verilerini çek ve kaydet
    public void saveHistoricalData(String symbol) {
        try {
            long now = Instant.now().getEpochSecond();
            long twoWeeksAgo = now - 14L * 24 * 60 * 60; // 14 gün önce

            String url = "https://finnhub.io/api/v1/stock/candle?symbol=" + symbol +
                    "&resolution=30&from=" + twoWeeksAgo + "&to=" + now + "&token=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            String status = fieldSafe(json, "s");
            if(!"ok".equalsIgnoreCase(status)) {
                System.out.println("[SKIP] " + symbol + " - market closed or no data");
                return;
            }

            JsonArray timestamps = json.getAsJsonArray("t");
            JsonArray opens = json.getAsJsonArray("o");
            JsonArray highs = json.getAsJsonArray("h");
            JsonArray lows = json.getAsJsonArray("l");
            JsonArray closes = json.getAsJsonArray("c");
            JsonArray volumes = json.getAsJsonArray("v");

            for(int i=0; i<timestamps.size(); i++) {
                long ts = timestamps.get(i).getAsLong();
                LocalDateTime timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(ts), NASDAQ_ZONE);

                // Duplikeleri önle
                if(repository.existsBySymbolAndTimestamp(symbol, timestamp)) continue;

                StockData data = new StockData();
                data.setSymbol(symbol);
                data.setOpen(opens.get(i).getAsDouble());
                data.setHigh(highs.get(i).getAsDouble());
                data.setLow(lows.get(i).getAsDouble());
                data.setClose(closes.get(i).getAsDouble());
                data.setVolume(volumes.get(i).getAsLong());
                data.setTimestamp(timestamp);

                repository.save(data);
            }

            System.out.println("[DONE] Historical data saved for: " + symbol);

        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Listedeki tüm semboller için çek
    public void fetchAndSaveAll(List<String> symbols) {
        for(String symbol : symbols) {
            saveHistoricalData(symbol);
            try { Thread.sleep(1200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}
