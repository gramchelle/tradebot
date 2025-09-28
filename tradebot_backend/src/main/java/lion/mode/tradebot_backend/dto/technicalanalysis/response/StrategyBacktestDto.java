package lion.mode.tradebot_backend.dto.technicalanalysis.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyBacktestDto {

    // Summary report of backtest results for a strategy on a specific symbol
    private String symbol;
    private String strategyName;
    private int lookbackPeriod;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String parametersJson; 

    // karar
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String currentSignal;
    private double score;
    private double lastDecisionValue;
    private String lastDecisionValueDescriptor; // confluence stratejilerde çalışır

    // performans
    private double totalProfit; // stratejinin toplam karı
    private double totalLoss; // stratejinin toplam zararı

    private double totalProfitLoss; // toplam kar/zarar oranı  
    private double totalProfitLossRatioPercent; // yüzde olarak

    private double grossReturn; // toplam getiri
    private double averageProfit; // ortalama kar
    private double breakEvenCount; // break even trades: trades with no profit no loss

    // private double profitCount; // kar eden işlemler
    // private double lossCount; // zarar eden işlemler

    // risk ve risk ayarlamaları
    private double rewardRiskRatio;
    private double maximumDrawdown;  //en yüksek tepe-dip farkı
    private double averageDrawdown; // tüm drawdown periyotlarının ortalaması

    // trade ve pozisyonlar
    private int numberOfTrades; // kapalı işlem sayısı
    private int numberOfPositions; // açılan pozisyon sayısı
    private double averageHoldingPeriod; // pozisyon başına ortalama tutma süresi
    private double totalHoldingPeriod; // tüm pozisyonların toplam tutma süresi (bar)

    // başarı oranları
    private double winningPositionsRatio; // karla kapanan pozisyonların oranı
    private double losingPositionsRatio; // zararla kapanan pozisyonlar
    private double winningTradesRatio; // toplam işlemlerde kazanma oranı

    // diğer metrikler
    private double buyAndHoldReturn; // sadece al ve tut stratejisi getirisi
    private double versusBuyAndHold; // stratejinin buy and hold'a karşı performansı
    private double cashFlow; // son bar bazında stratejinin nakit akışı
    private double profitLoss; //  normalize edilmiş kar/zarar

    // risk ayarlanmış metrikler
    private double sharpeRatio; // risk başına getiri oranı, yani getiri volatilitesi, yani standart sapma  
    private double sortinoRatio; //negatif volatiliteye göre ayarlanmış Sharpe oranı
}