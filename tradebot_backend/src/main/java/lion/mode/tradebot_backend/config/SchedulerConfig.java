package lion.mode.tradebot_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lion.mode.tradebot_backend.service.fetch_data_api.AlphaVantageDataCollectorService;
import lion.mode.tradebot_backend.service.fetch_data_api.FinnhubDataCollectorService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    //private final FinnhubDataCollectorService dataCollectorService;
    private final AlphaVantageDataCollectorService dataCollectorService;

    @Value("${tradebot.tracked-symbols-nasdaq}")
    private List<String> symbols;

    @Scheduled(fixedRate = 300000)
    public void scheduleDataFetch() {
        System.out.println("[START] Scheduled data fetch starting at " + LocalDateTime.now());

        //dataCollectorService.fetchAndSaveAll(symbols);

        for (String symbol : symbols) {
            dataCollectorService.saveStockData(symbol);
        }

        System.out.println("[DONE] Scheduled data fetch finished at " + LocalDateTime.now());
    }
}
