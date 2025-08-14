package lion.mode.tradebot_backend.config;

import lion.mode.tradebot_backend.service.DataCollectorService;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final DataCollectorService dataCollectorService;

    @Scheduled(fixedRate = 180000) // her 3 dakikada bir çalışır
    public void scheduleDataFetch() {
        dataCollectorService.getAllStockData();
        System.out.println("[DONE] Data fetched at " + java.time.LocalDateTime.now());
    }
}