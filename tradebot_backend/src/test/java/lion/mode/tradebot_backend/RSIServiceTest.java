package lion.mode.tradebot_backend;

import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lion.mode.tradebot_backend.service.technicalanalysis.indicators.RSIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RSIServiceTest {

    private StockDataRepository repository;
    private RSIService rsiService;

    @BeforeEach
    void setUp() {
        repository = mock(StockDataRepository.class);
        rsiService = new RSIService(repository);
    }

    @Test
    void testLoadSeriesWithMockData() {
        StockData s1 = new StockData();
        s1.setId(1L);
        s1.setSymbol("TEST");
        s1.setOpen(100);
        s1.setHigh(105);
        s1.setLow(95);
        s1.setClose(102);
        s1.setVolume(1000);
        s1.setTimestamp(LocalDateTime.of(2025, 9, 1, 0, 0));

        StockData s2 = new StockData();
        s2.setId(2L);
        s2.setSymbol("TEST");
        s2.setOpen(102);
        s2.setHigh(108);
        s2.setLow(101);
        s2.setClose(107);
        s2.setVolume(1500);
        s2.setTimestamp(LocalDateTime.of(2025, 9, 2, 0, 0));

        List<StockData> mockData = Arrays.asList(s1, s2);

        when(repository.findBySymbolOrderByTimestampAsc("TEST")).thenReturn(mockData);
        when(repository.findBySymbol("TEST")).thenReturn(List.of(s1));

        // To run the script below, make loadSeries(symbol) method public in IndicatorService
        /*
        BarSeries series = rsiService.loadSeries("TEST");

        assertNotNull(series);
        assertEquals(2, series.getBarCount());

        assertEquals(100, series.getBar(0).getOpenPrice().doubleValue());
        assertEquals(105, series.getBar(0).getHighPrice().doubleValue());
        assertEquals(95, series.getBar(0).getLowPrice().doubleValue());
        assertEquals(102, series.getBar(0).getClosePrice().doubleValue());
        assertEquals(1000, series.getBar(0).getVolume().doubleValue());

        series.getBarData().forEach(bar -> {
            System.out.println("Bar end time: " + bar.getEndTime() + ", close: " + bar.getClosePrice());
        });*/
    }
}
