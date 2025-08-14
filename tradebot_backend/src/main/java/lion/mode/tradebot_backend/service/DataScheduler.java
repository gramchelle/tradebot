package lion.mode.tradebot_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataScheduler {

    private final DataCollectorService dataCollectorService;

    @Scheduled(fixedRate = 180000) // fetch data per 3 minutes
    public void fetchDataPerMinute(){
        System.out.println("[DONE] Alpha Vantage'tan veri çekiliyor...");
        dataCollectorService.fetchStockData();
    }

}
