package lion.mode.tradebot_backend.dto.technicalanalysis.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyBacktestDto {

    /* Summary report of backtest results for a strategy on a specific symbol */

    private String symbol;
    private String strategyName;
    private int lookbackPeriod;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String parametersJson; // parametreleri veritabanına kaydetmek için

    // trade ve pozisyonlar
    private int tradeCount;               // kapalı işlem sayısı
    private int positionCount;            // açılan pozisyon sayısı
    private double breakEvenCount;        // break even trades: trades with no profit & no loss
    private double averageHoldingPeriod;  // pozisyon başına ortalama tutma süresi
    private double totalHoldingPeriod;    // tüm pozisyonların toplam tutma süresi (bar)

    // performans
    private double totalProfit;
    private double totalLoss;
    private double totalProfitLoss;
    private double totalProfitLossRatioPercent;
    private double averageProfit;

    private double grossReturn; // toplam getiri

    // risk ve risk ayarlamaları
    private double rewardRiskRatio;
    private double maximumDrawdown;  //en yüksek tepe-dip farkı

    // başarı oranları
    private double winningPositionsRatio; // karla kapanan pozisyonların oranı, decimal format
    private double losingPositionsRatio;  // zararla kapanan pozisyonlar oranı, decimal format

    private double buyAndHoldReturn; // sadece al ve tut stratejisi getirisi, yani stratejinin başlangıç ve bitiş fiyatları arasındaki fark
    private double versusBuyAndHold; // stratejinin buy and hold'a karşı performansı: pozitif ise daha iyi, negatif ise daha kötü
    private double cashFlow; // son bar bazında stratejinin nakit akışı
    private double profitLoss; //  normalize edilmiş kar/zarar

    // risk ayarlanmış metrikler
    private double sharpeRatio; //risk başına getiri oranı, yani getiri volatilitesi, daha yüksek değer daha iyi
    private double sortinoRatio; //negatif volatiliteye göre ayarlanmış Sharpe oranı, daha yüksek değer daha iyi

    // karar
    private double lastDecisionValue; // -1.0 ile 1.0 arasında
    private String lastDecisionValueDescriptor; // "Strong Buy", "Buy", "Hold", "Sell", "Strong Sell"
}