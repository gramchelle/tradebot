package lion.mode.tradebot_backend.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockDataConsumer {

    private final StockDataRepository repository;

    // Kafka'dan gelen mesajları işler ve veritabanına kaydeder
    @KafkaListener(topics = "${kafka.topic.stock-data}", groupId = "tradebot-group")
    public void consume(StockData data) {
        /*
        if (!repository.existsBySymbolAndTimestamp(data.getSymbol(), data.getTimestamp())) {
            repository.save(data);
            System.out.println("[DB Writer] Kaydedildi: " + data.getSymbol() + " @ " + data.getTimestamp());
        } else {
            System.out.println("[DB Writer] Zaten var: " + data.getSymbol() + " @ " + data.getTimestamp());
        }
            */
        repository.save(data);
    }
}
