package lion.mode.tradebot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lion.mode.tradebot_backend.model.StockData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockDataProducer {

    private final KafkaTemplate<String, StockData> kafkaTemplate;

    @Value("${kafka.topic.stock-data}")
    private String topic;

    // Kafka'ya mesaj gönderir
    public void sendStockData(StockData data) {
        kafkaTemplate.send(topic, data);
    }
}
