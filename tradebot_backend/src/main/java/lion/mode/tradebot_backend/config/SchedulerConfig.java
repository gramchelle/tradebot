package lion.mode.tradebot_backend.config;

import lion.mode.tradebot_backend.service.DataCollectorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfig {

    private final DataCollectorService dataCollectorService;

    public SchedulerConfig(DataCollectorService dataCollectorService) {
        this.dataCollectorService = dataCollectorService;
    }

    @Scheduled(fixedRate = 60000) // her 60 saniyede bir çalışır
    public void scheduleDataFetch() {
        dataCollectorService.fetchStockData();
        System.out.println("Data fetched at " + java.time.LocalDateTime.now());
    }
}