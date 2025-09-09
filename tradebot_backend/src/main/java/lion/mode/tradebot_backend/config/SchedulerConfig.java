package lion.mode.tradebot_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lion.mode.tradebot_backend.service.fetchdata.AlphaVantageDataCollectorService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final AlphaVantageDataCollectorService dataCollectorService;

    @Value("${tradebot.tracked-symbols-nasdaq}")
    private List<String> symbols;

    @Scheduled(fixedRate = 86400000) // executes per 24 hours
    public void scheduleDataFetch() {
        System.out.println("[START] Scheduled data fetch starting at " + LocalDateTime.now());

        for (String symbol : symbols) {
            dataCollectorService.saveStockData(symbol);
        }

        System.out.println("[DONE] Scheduled data fetch finished at " + LocalDateTime.now());
    }
}
