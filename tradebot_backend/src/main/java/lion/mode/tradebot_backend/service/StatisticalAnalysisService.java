package lion.mode.tradebot_backend.service;

import lion.mode.tradebot_backend.dto.statistical.PairsTrading;
import org.springframework.stereotype.Service;

import lion.mode.tradebot_backend.dto.statistical.CorrelationResult;
import lion.mode.tradebot_backend.dto.statistical.MeanReversionResult;
import lion.mode.tradebot_backend.model.StockData;
import lion.mode.tradebot_backend.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

@Service
@RequiredArgsConstructor
public class StatisticalAnalysisService {

    private final StockDataRepository stockDataRepository;

    // FR-03: Implement Mean Reversion Function
    public MeanReversionResult calculateMeanReversionSignal(String symbol, int period) {
        List<StockData> dataList = stockDataRepository.findBySymbolOrderByTimestampAsc(symbol); // Veritabanından verileri kronolojik sırada çek. -> TBD: Verileri kronolojik kaydet
        MeanReversionResult result = new MeanReversionResult();

        if (dataList.size() < period) {
            return new MeanReversionResult(); // Yeterli veri var mı (kafkaya geçince kaldırılacak)
        }

        List<Double> recentClosePrices = dataList.stream() // Sadece son 'period' kadar verinin kapanış fiyatlarını al
                .skip(dataList.size() - period) // Son 'period' veriyi almak için öncekileri atla
                .map(StockData::getClose)
                .collect(Collectors.toList());

        DescriptiveStatistics stats = new DescriptiveStatistics(); // apache commons math kütüphanesinden DescriptiveStatistics, istatistiksel hesaplamalar için kullanılır
        recentClosePrices.forEach(stats::addValue); // Kapanış fiyatlarını istatistik nesnesine ekle

        double mean = stats.getMean(); // Ortalama
        double stdDev = stats.getStandardDeviation(); // Standart Sapma

        // Standart sapma sıfır ise (tüm fiyatlar aynıysa), Z-skoru hesaplanamaz.
        if (stdDev == 0) {
            result.setSignalScore(0.0); // Nötr sinyal
            return result;
        }

        double currentPrice = dataList.get(dataList.size() - 1).getClose(); // kapanış fiyatı

        double zScore = (currentPrice - mean) / stdDev; // z skorunu hesaplar

        // Adım 3.5: Z-Skorunu -1 ile +1 arasında bir sinyal skoruna dönüştür.
        // Math.tanh fonksiyonu, Z-skoru arttıkça -1'e, azaldıkça +1'e yaklaşan
        // pürüzsüz bir S-eğrisi oluşturduğu için bu dönüşüm için idealdir.
        // Z-skoru pozitif (pahalı) -> tanh pozitif -> signalScore negatif (Sat)
        // Z-skoru negatif (ucuz) -> tanh negatif -> signalScore pozitif (Al)
        double signalScore = -Math.tanh(zScore);

        result.setSymbol(symbol);
        result.setPeriod(period);
        result.setMean(mean);
        result.setStdDev(stdDev);
        result.setCurrentPrice(currentPrice);
        result.setZScore(zScore);
        result.setSignalScore(signalScore);

        return result;
    }

    public CorrelationResult calculateCorrelation(String symbolA, String symbolB, int period) {
        List<StockData> dataA = stockDataRepository.findTopNBySymbolOrderByTimestampDesc(symbolA, period);
        List<StockData> dataB = stockDataRepository.findTopNBySymbolOrderByTimestampDesc(symbolB, period);

        if (dataA.size() < period || dataB.size() < period) {
            System.out.println("Insufficient data for one or both symbols.");
            return null;
        }

        // verileri hizala: verileri zaman damgalarına göre bir map'e koyarak eşleştirmeyi kolaylaştırıyoruz. best practicelerden biridir
        Map<LocalDateTime, Double> pricesAByTimestamp = dataA.stream()
                .collect(Collectors.toMap(StockData::getTimestamp, StockData::getClose));

        Map<LocalDateTime, Double> pricesBByTimestamp = dataB.stream()
                .collect(Collectors.toMap(StockData::getTimestamp, StockData::getClose));

        // Ortak zaman damgalarına sahip fiyatları tutacak listeler oluştur.
        List<Double> alignedPricesA = new ArrayList<>();
        List<Double> alignedPricesB = new ArrayList<>();

        // A listesindeki her bir zaman damgası için B listesinde karşılığı var mı diye kontrol et.
        pricesAByTimestamp.forEach((timestamp, priceA) -> {
            Double priceB = pricesBByTimestamp.get(timestamp);
            if (priceB != null) { // Eğer B'de de aynı zaman damgasına ait veri varsa...
                alignedPricesA.add(priceA);
                alignedPricesB.add(priceB);
            }
        });

        // korelasyon hesaplamak için en az 2 veri noktası olduğundan emin ol
        if (alignedPricesA.size() < 2) {
            System.out.println("Not enough common timestamps to calculate correlation.");
            return null;
        }

        // listeleri double türüne çevir
        double[] pricesArrayA = alignedPricesA.stream().mapToDouble(Double::doubleValue).toArray();
        double[] pricesArrayB = alignedPricesB.stream().mapToDouble(Double::doubleValue).toArray();

        // pearson korelasyon
        PearsonsCorrelation correlationCalculator = new PearsonsCorrelation();
        double correlation = correlationCalculator.correlation(pricesArrayA, pricesArrayB);

        CorrelationResult result = new CorrelationResult();
        result.setPair(symbolA+" "+symbolB);
        result.setSymbolA(symbolA);
        result.setSymbolB(symbolB);
        result.setPeriod(period);
        result.setCommonDataPoints(alignedPricesA.size());
        result.setCorrelation(correlation);
        return result;
    }

    // ?
    public PairsTrading calculatePairsTradingSignal(String symbolA, String symbolB, int period) {
        PairsTrading result = new PairsTrading();

        List<StockData> dataA = stockDataRepository.findTopNBySymbolOrderByTimestampDesc(symbolA, period);
        List<StockData> dataB = stockDataRepository.findTopNBySymbolOrderByTimestampDesc(symbolB, period);

        if (dataA.size() < period || dataB.size() < period) {
            System.out.println("Insufficient data for one or both symbols.");
            return null;
        }

        // Verileri hizala ve aralarındaki oranı (spread) hesapla. TreeMap kullanarak zaman damgalarını doğal olarak sıralı tutuyoruz.
        Map<LocalDateTime, Double> pricesAByTimestamp = new TreeMap<>(dataA.stream()
                .collect(Collectors.toMap(StockData::getTimestamp, StockData::getClose)));
        Map<LocalDateTime, Double> pricesBByTimestamp = new TreeMap<>(dataB.stream()
                .collect(Collectors.toMap(StockData::getTimestamp, StockData::getClose)));

        List<Double> spreadHistory = new ArrayList<>();
        pricesAByTimestamp.forEach((timestamp, priceA) -> {
            Double priceB = pricesBByTimestamp.get(timestamp);
            if (priceB != null && priceB != 0) { // Ortak zaman damgası varsa ve bölen sıfır değilse
                spreadHistory.add(priceA / priceB); // Spread'i (oranı) hesapla ve listeye ekle.
            }
        });

        if (spreadHistory.size() < 2) {
            return Map.of("error", "Not enough common data points to calculate spread Z-score.");
        }

        // Spread geçmişinin istatistikleri
        DescriptiveStatistics stats = new DescriptiveStatistics();
        spreadHistory.forEach(stats::addValue);
        double meanSpread = stats.getMean();
        double stdDevSpread = stats.getStandardDeviation();

        if (stdDevSpread == 0) {
            return new PairsTrading();
        }
        double currentSpread = spreadHistory.get(spreadHistory.size() - 1); // En son (şimdiki) spread değeri
        double zScore = (currentSpread - meanSpread) / stdDevSpread; // Şimdiki spread'in Z-Skoru

        // Z-Skoruna göre al/sat sinyali üret
        String signal;
        String actionDetail = "";
        // Genellikle 1.5 veya 2.0 standart sapma eşik olarak kullanılır.
        if (zScore > 1.5) {
            // Makas anormal derecede açılmış. Ortalamaya döneceğini varsayıyoruz. Yani, pay (A) düşecek, payda (B) artacak.
            signal = "SELL_PAIR";
            actionDetail = "Sell " + symbolA + ", Buy " + symbolB;
        } else if (zScore < -1.5) {
            // Makas anormal derecede daralmış. Ortalamaya döneceğini varsayıyoruz. Yani, pay (A) artacak, payda (B) düşecek.
            signal = "BUY_PAIR";
            actionDetail = "Buy " + symbolA + ", Sell " + symbolB;
        } else {
            signal = "HOLD_PAIR";
            actionDetail = "Spread is within normal range.";
        }

        result.setPair(symbolA + "-" + symbolB);
        result.setMeanSpread(meanSpread);
        result.setStdDevSpread(stdDevSpread);
        result.setCurrentSpread(currentSpread);
        result.setZScore(zScore);
        result.setSignal(signal);
        result.setAction(actionDetail);
        return result;
    }

    // FR-04: Implement Z Score Deviation Function
    // FR-05: Implement Pairs Trading Function
    // FR-06: İstatistiksel analiz sonuçlarına göre -1 ile +1 arasında bir "AL/SAT" sinyali üreten bir mantık geliştirilmelidir.

}
