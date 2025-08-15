package lion.mode.tradebot_backend.config;

import lion.mode.tradebot_backend.service.DataCollectorService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final DataCollectorService dataCollectorService;
/*
    @Value("${tradebot.tracked-symbols")
    private List<String> symbols;

    @Scheduled(fixedRate = 300000) // this scheduler lets the statement get executed per 5 minute, which is safer for free API requests
    public void scheduleDataFetch() {
        System.out.println("[START] Scheduled data fetch starting at " + LocalDateTime.now());

        // 2. Listede tanımlı her bir sembol için bir döngü başlatıyoruz.
        for (String symbol : symbols) {
            System.out.println("--> Fetching data for: " + symbol);
            dataCollectorService.saveStockData(symbol);
            System.out.println("[DONE] Data fetched for " + symbol);

            try {
                Thread.sleep(15000); // wait for 15 secs between requests
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
        */
}