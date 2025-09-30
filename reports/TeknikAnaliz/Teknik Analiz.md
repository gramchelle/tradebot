# Teknik Analiz

Finans piyasasında şirketlerin hisselerinin (stock), fonların, dövizlerin ve benzeri diğer menkul kıymetlerin (security) satın alınıp satılmasına karar verebilmek üzere kullanılan birçok çeşit analiz yöntemleri bulunmaktadır. Bunlardan en yaygın bilinenleri

- Temel analiz (fundamental)
- Teknik analiz (technical)

çeşitleridir.

## Finansal Analiz Çeşitleri

### Temel Analiz
Bir şirketin gerçek değerini anlamak için finansal tablolar, endüstri analizleri, ekonomik göstergeler ve diğer çeşitli makroekonomik faktörleri inceleyen bir yöntemdir. [1]

### Teknik Analiz
Fiyat hareketlerini ve işlem hacimlerini inceleyerek gelecekteki piyasa trendlerini tahmin etmeye çalışır. Bu yaklaşım geçmiş piyasa verilerinin gelecekteki fiyat hareketlerini belirlemede yardımcı olabileceği varsayımına dayanır. Teknik analistler, grafikler ve çeşitli göstergeler kullanarak destek ve direnç seviyelerini, trend çizgilerini ve fiyat örüntülerini belirlerler. [1]

Bu raporda bir fiyat grafiği nasıl okunur, nasıl yorumlanır, teknik analiz nedir, teknik analiz aşamasında kullanılan göstergeler (indicators) nelerdir gibi konulardan bahsediyor olacağız. Analiz aracı olarak TradingView.com platformunu kullanacağız.

![Teknik Analiz](blog_ici_gorsel_teknik_analiz2-1.jpg)

Yukarıdaki görselde görüldüğü üzere bir fiyat grafiği üzerinde kullanılan çeşitli teknik analiz indikatörlerinin, grafik formasyonlarının ve diğer tüm yöntemlerin adeta bir orkestra yönetircesine ahenk içinde işlendiğini ve bir arada kullanılmasının aslında ne kadar karmaşık bir hal alabileceğini gözlemleyebiliriz. 

> Kısacası: Temel analiz bir şirketin neden değerli olduğuna bakarken, teknik analiz bu değerin fiyat grafiğine nasıl yansıdığına odaklanır.

### Temel Terminoloji

* **Trade:** Türkçesi ile takas, finansal piyasalarda alım-satım işlemleri yapma işleminin bütününe verilen isimdir.

* **Trader:** Finansal piyasalarda alım-satım işlemleri yapma işlemini yapan kişiye verilen isimdir. Birkaç çeşidi bulunmaktadır: Intraday Trader, Day Trader, Swing Trader.

    * Intraday Trader: Aynı gün içinde kısa vadeli işlemler yapar, günün sonunda tüm pozisyonlarını kapatır.

    * Day Trader: Günlük fiyat dalgalanmalarından faydalanarak alım-satım yapar, genellikle bir günden uzun pozisyon taşımaz.

    * Swing Trader: Orta vadeli dalgalanmalardan faydalanır, pozisyonlarını birkaç gün ile birkaç hafta arasında tutabilir.

* **Stock:** Hisse

* **Security:** Menkul varlık (Hisse, Döviz, Fon, Emtia vb.)

* **Hacim**: Borsada hacim, belirli bir zaman diliminde (gün, saat, dakika) bir hisse senedi veya finansal enstrüman üzerinde gerçekleşen işlem miktarını ifade eder.

* **Indicator:** Geçmiş fiyat ve hacim verilerini kullanarak piyasanın yönü, momentumu veya gücü hakkında ipuçları veren teknik analiz araçlarıdır. Yatırımcılara alım, satım veya bekleme kararlarında yardımcı olur.

* **Buy/Sell/Hold Signals:** Al/Sat/Tut sinyalleri, yapılan teknik analiz sonucunda bir hissenin alınıp alınmaması gerektiğine dair gönderilen sinyallere denir.

* **Bullish:** Piyasanın veya bir varlığın fiyatının yükseleceği beklentisi. Yatırımcıların iyimser olduğu, alım baskısının yüksek olduğu dönemleri tanımlar. Türkçede genellikle “boğa piyasası” olarak geçer. Boğa benzetmesi, boğanın saldırı halinde kafasını yukarı kaldırarak saldırmasından kaynaklıdır, yükselişi temsil eder.

* **Bearish:** Piyasanın veya bir varlığın fiyatının düşeceği beklentisi. Yatırımcıların kötümser olduğu, satış baskısının yüksek olduğu dönemleri tanımlar. Türkçede genellikle “ayı piyasası” olarak geçer. Ayı benzetmesi, ayının saldırı halinde ellerini aşağı doğru savurarak saldırmasından kaynaklıdır, düşüşü temsil eder.

    ![Bull and Bear](image.png)

## Fiyat Grafiği Okuma

Bu kısımda fiyat grafiklerini okumayı, yorumlamayı ve mum grafiklerinin (candlesticks) anatomisini inceliyor olacağız.

![Garanti Bankası 1 Yıllık Günlük Bazda Fiyat Grafiği](price_chart.png)

Yukarıdaki fiyat grafiğinde Garanti Bankası'nın (GARAN) günlük bazda 1 yıllık (1 Day/1 Year) fiyat değişimi'ni gösteren TradingView görseli verilmiştir. [2] Ancak, bir hissenin fiyat grafiğini yorumlamadan önce OHLCV ve Symbol anahtar kelimeleri kesinlikle öğrenilmelidir.

### OHLCV Nedir?

OHLCV, Open High Low Close Volume kelimelerinin kısaltımını temsil etmektedir. Bu kelimeler ise:

* **O**pen: Fiyatın belirlenen zaman aralığındaki **açılış** fiyatını gösterir.
* **H**igh: Fiyatın belirlenen zaman aralığında ulaşmış olduğu **en yüksek** fiyatı gösterir.
* **L**ow: Fiyatın belirlenen zaman aralığında ulaşmış olduğu **en düşük** fiyatı gösterir.
* **C**lose: Fiyatın belirlenen zaman aralığındaki **kapanış** fiyatını gösterir.
* **V**olume: Fiyatın belirlenen zaman aralığındaki **hacmini** gösterir.
* Symbol: Hissenin (veya menkul varlığın) sembolünü gösterir. (Örn. APPLE - AAPL)

Bu kavramlar finans piyasasının bel kemiği görevini görürler ve bir finansal analist veya bir yatırımcı tüm süreçlerinde bu kavramları dahil edeceğinden dolayı bu kavramlara oldukça hakim olmalıdır. Raporun ilerleyen kısımlarında bahsedeceğimiz teknik analiz indikatörlerinde bu değerleri sıklıkla kullanacağız. Peki biz bu değerleri nasıl okur ve değerlendiririz? Öğrenme sürecimize bir mum grafik anatomisini inceleyerek başlayabiliriz.

## Mum Grafik Anatomisi

Mum grafik, borsada (candlestick olarak da bilinen) bir grafik türüdür. Menkul kıymetler üzerinde gerçekleşen fiyat değişimlerini temsilen kullanılır ve analistler tarafından incelenmek üzere kullanılır. En yaygın kullanılan grafik türlerinden bir tanesidir.

Peki, bir yatırımcı olarak biz mum grafikleri nasıl okuruz? İncelemek üzere aşağıda bir mum grafiğin anatomisi yer almaktadır. [3]

![Mum Grafiği Anatomisi](candlestick_chart.png)

Mum grafikler çoğunlukla fiyat grafiklerinde yeşil ve kırmızı olarak yer alır. Yeşil grafikler bir artışı (bullish) simgelerken , kırmızı grafikler fiyatta düşüşü (bearish) simgeler. Bir mum grafiğinin;

* **Open** değeri, **Close** değerinden düşük ise, yani fiyat kapanışa doğru artmışsa, bu bir artış (bullish) grafiğini temsil eder. Yani mum, yeşil renk alır.

* **Close** değeri, **Open** değerinden düşük ise, yani fiyat kapanışa doğru azalmışsa, bu bir azalış (bearish) grafiğini temsil eder. Yani mum, kırmızı renk alır.

Bir mumun açılış noktası, diğer mumun kapanış noktasıdır.

İkinci en yaygın kullanılan grafik türü olan bar grafikleri inceleyelim.  

## Bar Grafikler

Bar grafikler de fiyat değişimlerini temsil etmek için kullanılan en yaygın grafik türlerinden birisidir. Mantığı mum grafiklerden çok farklı olmamak üzere; en tepe nokta High, en düşük nokta Low değerlerini temsil eder. Barın türüne göre (yani boğa veya ayı grafiğini temsil etmesine göre), eğer yeşilse (artışı temsil ediyorsa) high değerinin altında kalan veya high değeriyle kesişen en üstteki değer kapanış değerini yani **Close**'u, alttaki çizgi ise hissenin o zaman aralığındaki açılış yani **Open** değerini temsil eder. Düşüş temsil eden ayı (bearish) grafiklerinde de tam tersi durum geçerlidir. Close ile Open yer değiştirir, yani menkul o zaman aralığı içerisinde değer kaybetmiştir ve kapanış değeri açılış değerinin altına düşmüştür. Aşağıda bullish bir bar grafiğinin anatomisi (boğa barı) yer almaktadır.

![Bar Chart](bar_chart.png)

Bu raporda mum grafikler kullanılarak ilerlenecektir. Grafiklerin analizi yaparken dikkat edilmesi gereken en önemli hususlardan bir diğeri ise zaman aralığı (time interval)'dır.

| "Timing is everything.", *Technical Analysis for Dummies, 2004, Rockefeller B.*

Barbara Rockefeller'ın da dediği gibi, borsada zamanlama her şeydir, tıpkı hayattaki çoğu konuda olduğu gibi. Farklı türde yatırımcıların kullandığı farklı zaman aralıkları bulunmaktadır. Örneğin, kısa vadeli küçük kazançlar elde etmek isteyen borsacılar (intraday ya da day trader'lar) daha küçük zaman aralıkları kullanarak analiz işlemlerini gerçekleştirirken, uzun vadede getiri elde etmek isteyen borsacılar daha büyük (1 gün, 1 hafta veya 1 ay gibi) zaman aralıkları kullanarak analizlerini gerçekleştirirler.

| Trader Tipi         | Tercih Edilen Zaman Aralığı       |
|---------------------|-----------------------------------|
| **Intraday**        | 1 dk – 15 dk – 1 saat             |
| **Day Trader**      | 15 dk – 1 saat – 4 saat           |
| **Swing**           | 4 saat – Günlük                   |
| **Long Position**   | Haftalık – Aylık                  |

Farklı trading türlerinin çoğunlukla tercih ettiği fiyat zaman aralıkları yukarıdaki tabloda özetlenmiştir.

### Zaman Aralıkları Nasıl Kullanılır?

Araştırma aracımız olan TradingView platformunda yer alan zaman aralıkları 1 saniye ile 12 ay arasında değişmektedir.

![Saatlik Veri](hourly_data.png)
TradingView - GARAN 1 saat bazlı 1 yıllık fiyat değişimi [2]

![Günlük Veri](daily_data.png)
TradingView - GARAN 1 gün bazlı 1 yıllık fiyat değişimi [2]

Yukarıdaki grafiklerden de anlaşılabildiği üzere kullanılan zaman aralığı grafiğin granülerliğini de büyük ölçüde etkilemektedir. Kullanılan aralık küçüldükçe (örn. 1 gün -> 1 saat) granülerlik artar ve daha anlık veri akışı sağlanır. Intraday traderlar daha granüler (zaman aralığı daha küçük) bir fiyat grafiği kullanırlar.

Fiyat grafikleri okuma ve yorumlama yapılmadan bunların öğrenilmesi kritik önem taşımaktadır. Bir sonraki bölümde, öğrendiğimiz OHLCV verilerini kullanarak geçmişe dayalı geleceğe yönelik tahmin yapmamızda yardımcı olacak olan teknik analiz aşamasına geçeceğiz.

## Teknik Analize Giriş

![Teknik Analiz Karikatür](ta_comic.png) [7]

Teknik analiz, borsada veya finansal piyasalarda **fiyatların geçmişine bakarak gelecekte neler olabileceğini tahmin etmeye** çalışan bir yöntemdir.Aynı zamanda piyasa riskini kontrol edebilmeyi de içerir [7].

Aslında bu, günlük hayatta da yaptığımız bir şeye benzer:  
- Havanın son birkaç gündür bulutlu ve rüzgârlı olduğunu görüyorsak, yarın yağmur yağma ihtimalinin yüksek olduğunu düşünürüz.  
- Bir mağazanın indirim dönemlerinde fiyatlarını nasıl değiştirdiğini gözlemliyorsak, gelecek ay yine benzer bir kampanya yapabileceğini tahmin ederiz.

Teknik analiz de benzer şekilde, “geçmişte fiyat böyle hareket etmişse, gelecekte de benzer şekilde hareket edebilir” varsayımına dayanır.

![Teknik Analiz Trend](teknik-analiz-trend.jpg)

Bu görselde teknik analizin en basit yapı taşlarından birisi olan trend çizgileri ile oluşturulan 3 farklı trend çeşidini gözlemliyoruz: Düşüş, yatay ve ayı trendleri. [5]

### Teknik analizde temel amaç nedir?
- Bir hissenin, dövizin veya herhangi bir yatırım aracının yükselme (bullish) ya da düşme (bearish) ihtimalini önceden tahmin edebilmek.  
- Yatırımcıya “ne zaman almalı, ne zaman satmalı, ne zaman beklemeli” konusunda yol göstermek.  

### Teknik analiz neye odaklanır?
- Fiyat grafikleri (OHLCV verileri)
- İşlem hacimleri (bir üründen ne kadar alınıp satıldığı)  
- Çeşitli göstergeler (indicators)

Bu veriler sayesinde yatırımcılar, piyasadaki *fırsatları* veya *riskleri* daha kolay görmeye çalışır.  

### Teknik Analiz Çeşitleri (Stratejiler)

![Technical Analysis Types](ta_types.png)

## İndikatörler

İndikatörler, geçmiş fiyat ve işlem hacmi verilerini kullanarak piyasadaki trendleri, momentumları ve olası dönüş noktalarını göstermek için kullanılan araçlardır. Yatırımcılar, indikatörleri kullanarak **alım, satım veya bekleme kararları** verir. En yaygın indikatörler arasında Hareketli Ortalamalar (MA), RSI, MACD ve Bollinger Bantları bulunur.

Aşağıdaki görselde fiyat grafiğinin üstünde, grafik üzerinde ve altında olacak şekilde 3 farklı teknik analiz indikatörünün nasıl kullanılabileceği hakkında görsel açıdan bir fikir edinebiliriz.

İndikatörler sırasıyla RSI, SMA ve MACD göstergeleridir.

![Technical Indicators](technical_indicators.png)

Bu göstergeler fiyatın hareketliiği, likiditesi, oynaklığı ve momentumu hakkında fikir elde etmemizi sağlayan bazı algoritmaların grafiksel sonuçlarıdır. 

Burada bilinmesi gereken en önemli unsur:

> Hiçbir indikatör tek başına güvenilerek kullanılmamalıdır. En az iki indikatör kombine halde veya stratejiye dayalı olarak bir indikatör trend ile birleştirilerek kullanılırsa daha sağlıklı tahminler yapılabilir.

### 1. Trendlines
Fiyat hareketlerindeki belirgin veya gizli yükseliş ve düşüşleri göstermek için iki nokta arasına çizilen düz çizgilerdir. Trendlines, piyasadaki genel yönü (yükselen, düşen veya yatay) hızlıca anlamamızı sağlar. Trend çizgileri seçilen zaman aralığındaki fiyat değişim grafiğindeki mum grafiklerin açılış ve kapanış noktalarına göre çizilir. Aşağıdaki görselde Garanti Bankası'nın 1 saatlik bazda 3 aylık fiyat değişim grafiği yer almaktadır (1h-3M). Bazı örnek trend çizgileri çizilmiştir. 

![GARAN 3 aylık Trendlines](trendlines_garanti.png)

Trendler teknik analiz konusunda borsanın en önemli bileşenlerinden biridir. Trend çizgileri ise fiyatın gidişatı hakkında fikir sahibi olmamıza yarayacak olan çok önemli bir indikatör tipidir.

### Trendlerin Temel Özellikleri

Trendleri anlamak için yalnızca çizgiler değil, yön, güç ve kırılma gibi kavramlar da önemlidir. Aşağıda trendlerin temel bileşenleri özetlenmiştir:

1. **Trendin Yönü**
   - **Yükseliş trendi (Uptrend):** Fiyatlar daha yüksek zirveler (higher highs) ve daha yüksek dipler (higher lows) yapar.
   - **Düşüş trendi (Downtrend):** Fiyatlar daha düşük zirveler (lower highs) ve daha düşük dipler (lower lows) yapar.
   - **Yatay trend (Sideways/Range):** Fiyat belli bir bant arasında sıkışır.

2. **Trend Çizgileri**
   - Yükseliş trendinde dipler birleştirilerek çizilir.
   - Düşüş trendinde zirveler birleştirilerek çizilir.
   - Çizgiler fiyatın gelecekte hangi seviyelerde duraksayabileceğini gösterebilir.

3. **Trendin Gücü**
   - **Hacim (volume):** Yüksek hacimde devam eden trend daha güçlüdür.
   - **ADX gibi indikatörler:** Trendin gücünü ölçmek için kullanılabilir. (Raporun ilerleyen kısımlarında bahsedeceğiz.)

4. **Trendin Kırılması (Breakout)**
   - Fiyat trend çizgisini aşarsa mevcut trendin sona erdiği düşünülebilir.
   - Çoğu zaman kırılma sonrası yeni bir trend başlar.

5. **“Trend Dosttur” (The Trend is Your Friend)**
   - Yatırımcıların çoğu mevcut trend yönünde işlem yapmayı tercih eder.
   - Trend yönünde yapılan işlemler genellikle daha güvenli kabul edilir.

Trend çizgileri çoğu zaman bazen tek başına çok güvenilir bir anlam ifade etmese de, bazı grafik formasyonları (patternler), destek ve direnç çizgileri, diğer indikatörleri yorumlama gibi çeşitli konularda yardımcı olmaktadır. Trend çizgilerini yorumlamaya geçmeden önce basit bir trendline hesaplama algoritmasını inceleyelim.  

```pseudo
TrendlineHesaplama

Girdi: FiyatListesi, TrendPeriyodu

1. Fiyat grafiğinde TrendPeriyodu kadar yüksek ve düşük noktaları belirle
2. Yükselen trend için düşük noktaları birleştir
3. Düşen trend için yüksek noktaları birleştir
4. Trend çizgisini çiz ve eğimini belirle
5. Trendin yönünü tespit et (yükselen, düşen, yatay)

Çıktı: TrendEgimi

Bitir
```

Peki, yukarıda sözde kod ile yazdığımız TrendlineHesaplama algoritmasının ana çıktısı olan TrendYönü'nü nasıl yorumlarız?

En basit haliyle, eğer trend çizgimizin eğimi 0'dan yükseldikçe artış, düştükçe azalış gösterecektir. Basit bir güven aralığı ile trendin eğimine göre artış mı, azalış mı yoksa yatay trend mi olduğunu hesaplayabiliriz. Aşağıda, GOOGL sembolünün günlük bazda yıllık fiyat verisi üzerinden **destek** çizgisi görevi gören bir trendline çizimini görebilirsiniz.

![Trendline - Support Line](trendline_support.png)

Yukarıdaki grafikte gördüğümüz 473 periyotluk bu destek, TradeBot tarafından aşağıdaki şekilde tespit edilmiştir:

```json
{
  "symbol": "GOOGL",
  "indicator": "Trend",
  "date": "2025-03-11T21:00:00Z",
  "values": {
    "nearestSupport": 190.68,
    "nearestResistance": 174.97,
    "recentSwingLow": 190.68,
    "recentSwingHigh": 174.97,
    "slopeAtDate": -124.64285714285705,
    "isUptrend": -1,
    "currentPrice": 167.11
  }
}
```

Trend çizgilerimizi oluşturmak için kullandığımız teknik ve kalıplar kadar, bunları nerede kullanacağımız ve hangi formasyonlar üzerinden yorumlama yapacağımız da önem taşımaktadır. Trend çizgileri envai çeşit alanda kullanılabilir ve çok farklı yorumlama teknikleri bulunabilir. En basit 3 trend yönü Artış, Azalış ve Sabit kalma trendleridir, ancak bunlarla yetinilmemelidir. Grafikler üzerinden trend kalıplarını yorumlamaya başlamak üzere bazı yaygın formasyonları inceleyelim. [6]

![Chart Formations](chart_formations.png) 

Yukarıdaki görselde de görüldüğü üzere piyasada bugüne dek gelen bazı kalıplaşmış trendler benzer sonuçlar vermektedir dolayısıyla bu trendler kalıplaştırılarak günümüzdeki yorumlama yöntemlerini de etkilemektedir. Trend kalıpları tek başına bir gösterge olarak kullanılmamalıdır ancak diğer güvenilir indikatör verileriyle desteklenmelidir.

Ayrıca, grafik formasyonları ezberlenerek fiyat grafiklerinde aranmamalıdır. Borsada teknik analiz yaparak tahmin yapma konusunda gelişmek için bolca örnek yapılmalı ve fiyat grafiği takip araçları kullanılarak eller grafikler üzerinde çizimler yaparak, geçmişe dayalı tahminde bulunarak kirletilmelidir.

### Trend Kalıpları

Trend kalıpları, fiyatın mevcut yönünün devam edip etmeyeceğini veya tersine dönüp dönmeyeceğini tahmin etmek için kullanılan grafik oluşumlarıdır (formasyonlarıdır). Yukarıdaki görselde de görülüyor olabileceği üzere, genellikle iki ana kategoriye ayrılır:

1. **Reversal (Geri Dönüş Kalıpları):** Mevcut trendin sona erdiğini ve fiyat yönünün tersine dönebileceğini işaret eder. Bazı örnekler:

    * **Head and Shoulders (Omuz-Baş-Omuz):** Genellikle yükseliş trendinin sonunu işaret eder.

    * **Double Top / Double Bottom (Çift Tepe / Çift Dip):** Fiyatın belirli bir seviyeyi geçememesiyle trendin zayıfladığını gösterir.

2. **Continuation (Devam Kalıpları):** Mevcut trendin bir süre konsolide olduktan sonra aynı yönde devam edeceğini gösterir.

    * **Flag (Bayrak):** Güçlü bir hareket sonrası kısa süreli yatay veya hafif ters yönlü konsolidasyon.

    * **Triangle (Üçgen):** Fiyat sıkışırken, kırılım sonrası mevcut trend yönünde devam etme eğilimi vardır.

    * **Pennant (Flama):** Bayrak formasyonuna benzer, daha kısa vadeli küçük bir konsolidasyon formudur.

* Trend kalıplarını kullanırken dikkat edilmesi gerekenler:

    * Kalıp zaman aralığına göre farklı güvenilirlik gösterebilir. **Uzun vadeli grafiklerde çıkan formasyonlar genellikle daha güçlü sinyaller üretir.**

    * Her zaman hacim (volume) ile desteklenmelidir. Örneğin, bir yükseliş trendinde fiyat yukarı kırıldığında hacmin de artması gerekir.

    * Diğer indikatörlerle birlikte kullanılmalıdır (RSI, MACD, ADX gibi).

### Destek ve Direnç

Destek ve direnç seviyeleri, teknik analizin en temel yapı taşlarından biridir. Fiyat hareketlerinin belirli seviyelerde durma, yön değiştirme veya zorlanma eğilimini ifade eder.

![Support and Resistance](support_and_resistance.png)

1. **Destek** Nedir? (Support)

Fiyatın aşağı yönlü hareketini durduran veya yavaşlatan seviyedir. Genellikle birden fazla kapanış değerinin kestiği kırılma noktalarını teğet geçen bir trend çizgisi ile sembolize edilir. Yatırımcılar bu seviyeyi “ucuz” olarak görüp alım yapmaya başlar. Dolayısıyla talep artar ve fiyatın daha fazla düşmesi engellenir.

    Örneğin: Bir hisse sürekli 100 TL’ye düştüğünde alıcı buluyorsa, 100 TL seviyesi destek kabul edilir.

2. **Direnç** Nedir? (Resistance)

Fiyatın yukarı yönlü hareketini durduran veya yavaşlatan seviyedir. Birden fazla açılış değerinin kestiği kırılma noktalarını teğet geçen bir trend çizgisi ile sembolize edilir. Yatırımcılar bu seviyeyi “pahalı” olarak görüp satış yapmaya başlar. Arz arttığı için fiyatın daha fazla yükselmesi zorlaşır, bu nedenle direnç olarak bilinir.

3. **Destek ve Direnç** Nasıl Belirlenir?

    * Geçmişte fiyatın sık sık dönüş yaptığı noktalar incelenir.

    * Grafiklerde trend çizgileri, yatay çizgiler veya hareketli ortalamalar yardımıyla belirlenebilir.

    * Hacim (volume) analizi ile birlikte daha güvenilir hale gelir.

Destek ve direnç seviyelerinin önemi, bu seviyelerin kırılması (breakout) yeni trendlerin başlangıcı olabileceğinden dolayı kritiktir. **Alım** kararları genellikle destek bölgelerinde, **Satım** kararları ise direnç bölgelerinde yapılır. Destek ve direnç seviyelerini hesaplayabilmek için örnek bir algoritma şu şekildedir:

```pseudo
Destek Direnç Hesaplama

Başla

Girdi: FiyatlarListesi, Periyot

Destek ve direnç çizgilerinin aranacağı periyot miktarında fiyat verisi alınır

For her fiyat noktasında:
    Eğer fiyat daha önce birden çok kez aynı seviyeden yukarı dönmüşse:
        O seviye = DESTEK
    
    Eğer fiyat daha önce birden çok kez aynı seviyeden aşağı dönmüşse:
        O seviye = DİRENÇ

Eğer fiyat DESTEK seviyesini aşağı kırarsa:
    Yeni destek seviyesi daha aşağıda aranır

Eğer fiyat DİRENÇ seviyesini yukarı kırarsa:
    Yeni direnç seviyesi daha yukarıda aranır

Çıktı: actsAsSupport, actsAsResistance

Bitir
```

Bu algoritmanın verdiği çıktı, diğer indikatör çıktılarının sonuçlarını desteklemek amaçlı kullanılarak daha güvenli bir al/sat/tut sinyali gönderen bir sistem geliştirilebilir. Örneğin, trend çizgisinin destek çizgisi olduğundan ve bu destek çizgisinin uzun zamandan sonra kırılması durumundan kullanıcının haberdar edilmesi kararlarını önemli ölçüde etkileyecektir. Çünkü bu, fiyatların dip yapabileceği anlamına da gelmektedir. Aşağıdaki GOOGL 1d/1Y grafiğinde örnek birer destek ve direnç çizgilerini gözlemleyebiliriz. Kırmızı olan çizgi fiyatın yukarı çıkmasını engelleyen bir direnç gibi davranırken, mavi olan support çizgisi bir nevi fiyatın uzun dönemde aşağı düşmesini engelleyen destek görevi görmektedir.

![GOOGL Support and Resistance](tv_support_and_resistance.png)

En basit haliyle trend çizgileri bu amaçlarla kullanılmakta ve bu şekilde hesaplanarak yorumlanmaktadır. 

---

### 2. Hareketli Ortalamalar Kesişimi / MA Crossover

**Hareketli ortalama (Moving Average)**, bir menkul varlığın belirli bir periyottaki (genellikle 20 ve 50 kullanılır) fiyatlarının ortalamasını hesaplayarak trendin yönünü gösteren bir indikatördür. Fiyat hareketlerindeki dalgalanmaları yumuşatarak (smoothing) genel eğilimin daha net görünmesini sağlar. **MA Crossover (Hareketli Ortalama Kesişimi)**, kısa vadeli ve uzun vadeli hareketli ortalamaların birbirini kesmesiyle oluşur.

En yaygın kullanılan üç hareketli ortalama türü şunlardır:

1. **Simple Moving Average (SMA):** Belirlenen periyottaki (n) fiyatların basit aritmetik ortalamasıdır. Örneğin, 10 günlük SMA, son 10 günün kapanış fiyatlarının toplamının 10’a bölünmesiyle elde edilir.

   ![SMA](sma.png)

2. **Weighted Moving Average (WMA):**  Son fiyatlara ağırlık vererek ortalama alır. Böylece son dönem fiyat hareketleri SMA’ya göre daha hızlı yansıtılır.

   ![WMA](wma.png)

3. **Exponential Moving Average (EMA):**  WMA’ya benzer şekilde yeni fiyatlara daha fazla önem verir, ancak ağırlıklandırmayı üstel (exponential) bir yöntemle yapar. Bu nedenle fiyat değişimlerine en hızlı tepki veren hareketli ortalama türüdür. Diğer indikatörlerde de yaygın olarak kullanılan bir hareketli ortalama türüdür.

   ![EMA](ema.png)

> MA Crossover iki farklı periyottaki hareketli ortalamaların kesişmesi durumudur. İki oluşum gerçekleşebilir: **Golden Cross ve Death Cross**.

- **Golden Cross:** Kısa vadeli MA (ör. 50 günlük) uzun vadeli MA’nın (ör. 200 günlük) üzerine çıktığında görülür, genellikle yükseliş sinyali olarak yorumlanır.  
- **Death Cross:** Kısa vadeli MA uzun vadeli MA’nın altına düştüğünde oluşur, genellikle düşüş sinyali olarak değerlendirilir.

GOOGL sembolü üzerinden günlük bazda yıllık fiyat grafiğini gösteren EMA çizgilerini inceleyelim:

![EMA Crossover](ema_crossover.png)

Yukarıdaki görselde bir **Golden Cross** ve **Death Cross** örneği gözlemlenebilir.  
- Uzun vadeli (200 günlük MA) kırmızı ile,  
- Kısa vadeli (50 günlük MA) lacivert ile gösterilmiştir.  

Kısa vadeli MA’nın uzun vadeli MA’yı yukarı yönlü kestiği mavi ok ile; kısa vadeli MA’nın uzun vadeli MA’yı aşağı yönlü kestiği nokta (death cross) kırmızı ok ile gösterilmiştir.  

Görüldüğü üzere, yukarı yönlü kesim (Golden Cross) gerçekleştiğinde fiyatlarda artış gözlenmiş; aşağı yönlü kesimde (Death Cross) ise düşüş yaşanmıştır.  

> **Önemli Not:** Bu örnekler yalnızca bariz durumları göstermektedir.  
Periyotları değiştirmek (örneğin 9-26 yerine 50-200 kullanmak) veya zaman aralığını değiştirmek (1 günlük yerine 1 haftalık) MA Crossover sonuçlarını önemli ölçüde etkiler.

Aslında buradaki ana mantık, kısa dönemli ortalamanın uzun dönemli ortalamayı ne kadar hızlı kesip trendin varolan stratejideki periyotlara göre hangi yönde ilerleyeceğini görmek üzerine kuruludur. Periyotlar uzun dönemli bir yatırımcı için genel olarak kısa periyot 50, uzun periyot 200 olacak şekilde ayarlıdır. Ancak bu süreler genellikle yatırımcının risk yönetimine ve stratejisine göre belirlenir.

### MA Crossover TradeBot İmplementasyonu

Aşağıda, MA Cross'un hesaplama algoritmasını bir sözde kod parçası ile inceleyelim. 

```pseudo
Başla MA_Crossover_Hesaplama

Girdi: Sembol, FiyatlarListesi, KısaMA_Periyot, UzunMA_Periyot, MA_Türü, FiyatTürü

1. Kısa MA = Son KısaMA_Periyot günün ortalama fiyatı (seçilen MA_Türüne göre)
2. Uzun MA = Son UzunMA_Periyot günün ortalama fiyatı (seçilen MA_Türüne göre)
3. Eğer Kısa MA, Uzun MA'yı yukarı keserse: Al sinyali
4. Eğer Kısa MA, Uzun MA'yı aşağı keserse: Sat sinyali
5. Sinyalleri listele ve gün gün takip et

Çıktı: ShortMADegeri, LongMADegeri

Bitir
```

Yukarıdaki GOOGL sembolü üzerindeki SMA Crossover'ın bullish trende geçtiği (golden cross) noktada TradeBot üzerinden gelen örnek bir sonuç aşağıdaki gibidir. 

````json
{
  "symbol": "GOOGL",
  "indicator": "MACrossover",
  "date": "2025-09-18T21:00:00Z",
  "values": {
    "shortMA": 208.7082,
    "longMA": 183.1369
  }
}
````

MA Crossover algoritmasının girdileri fiyatlar listesi ile kısa ve uzun vadeli hareketli ortalama periyotlarıdır. Her gün için bu iki ortalama hesaplanır, ve kullanıcının stratejisi üzerinde kullanılmak üzere strateji katmanına gönderilir. Çoğu strateji SMA50>SMA200 kesişim noktasında uzun vadeli pozisyon açma emri gönderen bir yapıdadır.

> Örneğin: Eğer **Golden Cross** tespit edilmişse, yapılan teknik analiz aşamasında yukarı yönlü bir artış olduğunu gösteren bir sinyal (buy) gönderilebilir. 

---

### 3. RSI (Relative Strength Index)

RSI, Türkçesiyle Göreceli Güç Endeksi, teknik analizde en sık kullanılan momentum göstergelerinden biridir. Fiyatların belirli bir periyottaki (genellikle 14 gün) yükseliş ve düşüş hızını ölçerek, fiyatın **aşırı alım** veya **aşırı satım** seviyelerinde olup olmadığını gösterir. 1978’de J. Welles Wilder Jr. tarafından geliştirilmiştir.

RSI, girilen periyottaki fiyatların ortalama getiri ve ortalama kazancının birbirine oranlanmasını işleyen bir algoritmadır.

![RSI Formula](rsi_formula.png)

Burada kullanılan formül, verilen periyodun bir fazlası ile hesaplamaya başlayarak ortalama getiri ve kayıpları hesaplayarak birbirine oranlayıp fiyatın o periyottaki göreceli gücünü hesaplar.

- RSI değeri 0 ile 100 arasında değişir. Genelleşmiş yorumlar
  - RSI > 70 → Aşırı alım bölgesi (fiyatın çok hızlı yükseldiği, yakında düzeltme/düşüş gelebileceği düşünülür),
  - RSI < 30> → Aşırı satım bölgesi (fiyatın çok hızlı düştüğü, tepki yükselişi gelebileceği düşünülür) şeklindedir.

Yatırımcının kendi belirlediği stratejisine göre pozisyon emri gönderilir, ancak çoğunlukla kullanılan strateji, RSI > 70 noktasında "Sat", RSI < 30 noktasında "Satın Al" sinyalleri göndermektir.

```pseudo
Başla RSI_Hesaplama

Girdi: FiyatSerisi, Periyot, Tarih, FiyatTürü

1. Verilen tarihe kadar olan fiyat listesini seri halinde al (verilen tarih varsayılan olarak bugündür)
2. Günlük değişim = Bugünkü Fiyat - Önceki Günün Fiyatı
3. Pozitif değişimleri (kazanç) ve negatif değişimleri (kayıp) ayır
4. Ortalama Kazanç = Son 'Periyot' günün kazançlarının ortalaması
5. Ortalama Kayıp = Son 'Periyot' günün kayıplarının ortalaması
6. RS = Ortalama Kazanç / Ortalama Kayıp
7. RSI = 100 - (100 / (1 + RS))
8. Her gün için RSI değerini hesapla ve son günün değerini yazdır

Çıktı: RsiDeger

Bitir
```

TradeBot, RSI'ı en basit halde implemente edecek ve yatırımcıların stratejilerine uygun olarak değiştirebilecekleri bir formatta şekilde geliştirilmiştir. Aşağıda GOOGL 1d/1Y bazda fiyat verileri ile 14 periyotluk RSI grafiği yer almaktadır.

![TradingView GOOGL RSI](googl_rsi.png)

İşaretle gösterilen 11-12-2024 tarihli günlük bazda fiyat grafiğinin TradeBot RSI çıktısı:

````json
{
  "symbol": "GOOGL",
  "indicator": "RSI",
  "date": "2024-12-10T21:00:00Z",
  "values": {
    "rsiValue": 75.25854873670565
  }
}
````

şeklindedir.

### RSI’da Uyumsuzluk (Divergence)

RSI fiyat hareketi ile indikatör arasındaki uyumsuzluklarla da analiz edilir.

* **Pozitif Divergence**    
    Fiyat düşerken RSI yükselir. Bu durumda fiyat düşüşü zayıflıyor olabilir, yukarı yönlü bir dönüş sinyali sayılabilir.

* **Negatif Divergence**    
    Fiyat yükselirken RSI düşer. Bu durumda yükseliş gücünü kaybediyor olabilir, aşağı yönlü bir dönüş gelebilir.

Aşağıdaki görselde bir pozitif uyumsuzluk yer almaktadır. Yani uyumsuzluğun bozulduğu ilk anda fiyatın yukarı yönlü ivmeli bir artış göstereceğini bekleyebiliriz.

GOOGL sembolü üzerinde pozitif ve negatif uyumsuzluk örnekleri aşağıdaki gibidir.

![TradingView RSI Divergence](tv_rsi_divergence.png)

Görselde de görüldüğü üzere RSI trendinin fiyat trendi ile ters yön göstermesinin ardından fiyat RSI trendi yönünde ilerlemiştir.

> Ancak, bu asla unutulmamalıdır ki, RSI tek başına kullanılmamalıdır; tıpkı diğer göstergeler gibi, trendler ve diğer indikatörlerle birlikte değerlendirildiğinde daha güvenilir sonuç verir.  

---

### 4. MACD (Moving Average Convergence Divergence)

MACD, Türkçe adıyla **Hareketli Ortalama Yakınsama Iraksama**, teknik analizde en sık kullanılan trend ve momentum göstergelerinden biridir. MACD, farklı uzunluklardaki iki **üstel hareketli ortalamanın (EMA)** farkını alarak fiyat hareketlerindeki momentum değişimlerini ortaya koyar. Bu sayede hem trendin yönünü hem de hızını yorumlamaya yardımcı olur. 1970’lerde Gerald Appel tarafından geliştirilmiştir.  

- **MACD Çizgisi (MACD Line):** Kısa vadeli EMA – Uzun vadeli EMA  (EMA12-EMA26)
- **Sinyal Çizgisi (Signal Line):** MACD çizgisinin 9 günlük EMA'sı
- **Histogram:** MACD ile Sinyal çizgisi arasındaki fark (momentum gücünü gösterir)

MACD ```12-period EMA - 26-period EMA``` ile hesaplanır. Periyotlar stratejiden stratejiye değişiklik gösterebilir, ancak bu değerler sıklıkla kullanılmaktadır.

- MACD çizgisi, Sinyal çizgisini **yukarı yönlü kesmeye başladığında** → Genellikle **al sinyali** olarak yorumlanır.  
- MACD çizgisi, Sinyal çizgisini **aşağı yönlü kesmeye başladığında** → Genellikle **sat sinyali** olarak yorumlanır.  
- Histogramın büyümesi → Momentumun arttığını, küçülmesi → Momentumun zayıfladığını gösterir.

Aşağıda, GOOGL sembolünün günlük bazda fiyat grafiğinin altında MACD grafiğini inceleyebiliriz.  

![TradingView GOOGL MACD](tv_googl_macd.png)

Yukarıdaki grafikte de açıkça görüldüğü üzere MACD çizgisinin Sinyal çizgisini kestiği yönde fiyatta paralel değişiklikler olmuştur. MACD algoritmasının TradeBot üzerinde uygulanması aşağıdaki akış şeklindedir:

```pseudo
Başla MACD_Hesaplama

Girdi: FiyatSerisi, KisaPeriyot, UzunPeriyot, SinyalPeriyodu Tarih, FiyatTürü

1. Verilen tarihe kadar olan fiyat listesini seri halinde al (verilen tarih varsayılan olarak bugündür, fiyat türü genelde kapanış fiyatı).
2. Kısa EMA hesapla: EMA_kisa = EMA(FiyatSerisi, KisaPeriyot)
3. Uzun EMA hesapla: EMA_uzun = EMA(FiyatSerisi, UzunPeriyot)
4. MACD değerini bul: MACD = EMA_kisa - EMA_uzun
5. MACD serisinden sinyal hattını hesapla: Signal = EMA(MACD, SinyalPeriyodu)
6. Histogramı hesapla: Histogram = MACD - Signal
7. Son gün için:
   MacdValue = MACD[son]
   SignalValue = Signal[son]
   HistogramValue = Histogram[son]

Çıktı: MacdValue, SignalValue, HistogramValue

Bitir
```

Bu algoritma kullanılarak TradeBot üzerinden gönderilen örnek bir çıktı aşağıdaki gibidir.

````json
{
  "symbol": "GOOGL",
  "indicator": "MACD",
  "date": "2025-06-05T21:00:00Z",
  "values": {
    "histogram": 0.03692346951490431,
    "macdLine": 2.709551266056451,
    "signalLine": 2.672627796541574
  }
}
````

### MACD’de Uyumsuzluk (Divergence)

MACD de RSI gibi, fiyat hareketiyle arasındaki **uyumsuzluklardan** (divergence) dolayı önemli sinyaller verebilir.  

- **Pozitif Divergence**  
  Fiyat düşerken MACD yükselir. Bu durum, düşüşün gücünü kaybettiğini ve yukarı yönlü dönüşün başlayabileceğini gösterir.  

- **Negatif Divergence**  
  Fiyat yükselirken MACD düşer. Bu, yükselişin zayıfladığını ve aşağı yönlü dönüş olabileceğini işaret eder.

Aşağıdaki grafikte GOOGL sembolünün günlük bazdaki fiyat verisi üzerinde MACD uyuşmazlıkları örneklerini inceleyebiliriz. MACD indikatörüyle uyumsuzluk bulmanın iki yöntemi vardır.

1. MACD tepelerini birleştirme
2. Histogram uçlarını birleştirme

Aşağıdaki örnekte iki yöntem de birden kullanılarak örnekler verilmiştir.

![MACD Divergence](macd_divergence.png)

**Önemli Not: MACD ≠ Golden/Death Cross**  
MACD kesişimleri (MACD Line – Signal Line) ile hareketli ortalama kesişimleri (Golden Cross / Death Cross) farklı kavramlardır.  

> MACD, tek başına kullanılmamalıdır. RSI, trend çizgileri ve hacim gibi diğer araçlarla birlikte değerlendirildiğinde çok daha güvenilir sonuçlar verir. 

---

### 5. Bollinger Bands

**Bollinger Bantları**, John Bollinger tarafından geliştirilmiş bir teknik analiz göstergesidir. Fiyatın ortalamasını ve standart sapmasını dikkate alarak üç çizgi oluşturur:  

- Orta bant → Basit hareketli ortalama (SMA) (genellikle 20 gün)  
- Üst bant → Orta bant + (Standart Sapma × çarpan)  
- Alt bant → Orta bant - (Standart Sapma × çarpan) 

Bollinger bantlarının ana mantığı, fiyatın üst ve alt bantlar arasında kalacağını ve ortalamaya mutlaka dönüleceğini (mean reversion) tahmin eden bir strateji indikatörüdür. Bantların arasındaki mesafe, fiyatın oynaklığına göre genişler veya daralır.  

- Fiyat üst banda yaklaştığında → Aşırı alım (geri çekilme olabilir)  
- Fiyat alt banda yaklaştığında → Aşırı satım (tepki yükselişi olabilir)  
- Bantlar daraldığında → Yakında sert bir hareket gelebilir (volatilite sıkışması/bollinger squeeze)  
- Bantlar genişlediğinde → Piyasada yüksek oynaklık (high volatility) vardır. 

yorumları yapılabilir.

Aşağıdaki GOOGL sembolünün günlük bazdaki fiyat grafiğine eklenmiş olan Bollinger Bands üzerinden Bollinger Squeeze ve üst bantın dışına çıkıldığında geri dönülme durumlarını inceleyebiliriz.

![TradingView Bollinger Bands](tv_bollinger_bands.png)

### Bollinger Bands Hesaplama

Bollinger Bands formülü aşağıdaki gibidir.

![Bollinger Bands Formula](bollinger-bands-formula.jpg)

Bollinger bands hesaplama mantığını bir sözde kod algoritması ile oluşturarak daha detaylı inceleyebiliriz.

```pseudo
Başla BollingerBandsHesaplama

Girdi: FiyatlarListesi, SMAPeriyot, StandartSapmaÇarpanı (varsayılan = 2), FiyatTürü

1. Verilen fiyat serisinden son 'SMAPeriyot' günün fiyatlarını al
2. Orta Bant (SMA) hesapla:
   OrtaBantDegeri = Ortalama(FiyatlarSonN, N = SMAPeriyot)
3. Standart sapmayı hesapla:
   StandartSapma = StdDev(FiyatlarSonN, N = SMAPeriyot)
4. Üst Bant = OrtaBantDegeri + (StandartSapma × Çarpan)
5. Alt Bant = OrtaBantDegeri - (StandartSapma × Çarpan)
6. YüzdeB hesapla: yüzdeB = (SonFiyat - AltBant) / (ÜstBant - AltBant)
7. Bant Genişliği hesapla:BantGenisligi = (ÜstBant - AltBant) / OrtaBantDegeri

Çıktı: UstBantDegeri, OrtaBantDegeri, AltBantDegeri, yüzdeB, BantGenisligi

Bitir
```

Yukarıdaki algoritmanın sonucunda yatırımcı, kendi risk yönetimini kullanarak bollinger bantlarını stratejisinde uygulayabilir. Örnek bir TradeBot çıktısı aşağıdaki gibidir.

````json
{
  "symbol": "GOOGL",
  "indicator": "Bollinger Bands",
  "date": "2025-09-17T21:00:00Z",
  "values": {
    "middle": 228.0575,
    "bandwidth": 30.656918236664126,
    "upper": 263.01520065379015,
    "lower": 193.09979934620986,
    "percentB": 0.8428786726766722
  }
}
````

> Bollinger Bantları tek başına kullanılmamalıdır. RSI, MACD ve trend analizleriyle birlikte değerlendirildiğinde çok daha güçlü sinyaller üretir.  

---

### 6. MFI (Money Flow Index)

**MFI**, yani **Para Akışı Endeksi**, teknik analizde kullanılan bir momentum göstergesidir ve hacim verilerini de dikkate alır. RSI’a benzer şekilde çalışır, ancak fiyat hareketlerinin yanında işlem hacmini de kullanarak daha güvenilir sinyaller verir. RSI, fiyat momentumu üzerine odaklanır ve fiyat hareketlerinin yönsel hızını ve büyüklüğünü hesaplarken MFI, hacmi ağırlıklandırma faktörü olarak entegre ederek fiyat değişimlerinin arkasındaki piyasa kuvvetini ölçer. [8] MFI, fiyat ve hacim birleşimi sayesinde RSI’ya göre daha güvenilir sinyaller sunabilir. 

- MFI 0 ile 100 arasında değişir.  
  - **80’in üzeri** → aşırı alım bölgesi (fiyatın yüksek ve hacimli bir şekilde yükseldiği, düzeltme gelebileceği düşünülür)  
  - **20’nin altı** → aşırı satım bölgesi (fiyatın düştüğü ve tepki yükselişi gelebileceği düşünülür)

Para Akış Endeksi hesaplanırken aşağıdaki formülden yararlanılır:

![MFI Hesaplama](mfi_hesaplama.png)

MFI yorumlama yöntemlerini anlamak, RSI'a çok benzer olduğundan, zor olmayacaktır.

1. **Aşırı alım / aşırı satım:**  
   - MFI 80 üzerindeyse → potansiyel satış fırsatı  
   - MFI 20 altındaysa → potansiyel alım fırsatı  

2. **Divergence (Uyumsuzluk):**  
   - Fiyat yeni zirve (higher high) yaparken MFI düşüyorsa → yükseliş trendi zayıflıyor olabilir  
   - Fiyat yeni dip (lower low) yaparken MFI yükseliyorsa → düşüş trendi zayıflıyor olabilir 

> Diğer indikatörler gibi MFI da tek başına karar verme yöntemi olarak kullanılmamalıdır.

MFI algoritmasını ise daha iyi anlayabilmek üzere TradeBot üzerindeki algoritma implementasyonunu inceleyelim:

```pseudo
Başla MFI_Hesaplama

Girdi: FiyatlarListesi, Periyot

1. Tipik Fiyat = (Yüksek + Düşük + Kapanış) / 3  
2. Para Akışı = Tipik Fiyat * Hacim  
3. Pozitif ve Negatif Para Akışlarını ayır  
   - Bugünkü Tipik Fiyat > Önceki Gün Tipik Fiyat → Pozitif  
   - Bugünkü Tipik Fiyat < Önceki Gün Tipik Fiyat → Negatif  
4. Para Akışı Oranı = Son 'Periyot' günün Pozitif / Negatif para akışları  
5. MFI = 100 - (100 / (1 + Para Akışı Oranı))  
6. Her gün için MFI değerini listele

Çıktı: MfiDegeri

Bitir
```

Algoritmadan da anlaşılacağı üzere, pozitif ve negatif para akışları da dahil olduğundan hacim de dahil olmaktadır ve daha kapsamlı bir gösterge konumuna gelmektedir. Ancak piyasanın hareketliliğini RSI kadar keskin göstermediği unutulmamalıdır. Aşağıdaki görselde GOOGL sembolünün günlük bazdaki fiyat grafiğini MFI indikatörü ile yorumlayalım. 

![TradingView GOOGL MFI](mfi_divergence.png)

Yukarıdaki sözde kod algoritması, yukarıdaki grafiğin üzerinde çekilen son trendin ilk günü için uygulandığında TradeBot tarafından elde edilen çıktı

````json
{
  "symbol": "GOOGL",
  "indicator": "MFI",
  "date": "2025-09-17T21:00:00Z",
  "values": {
    "mfiValue": 87.11040676070746
  }
}
````

şeklinde olmaktadır.

> MFI da indikatör olarak tek başına kullanılmamalı, diğer indikatörler ve trendlerle birlikte değerlendirilmelidir.

---

### 7. DMI (Directional Movement Index)

**DMI (Yönlü Hareket İndeksi)**, bir varlığın trend yönünü ve bu trendin gücünü ölçmeye yarayan bir göstergedir. Genellikle **ADX (Average Directional Index)** ile birlikte kullanılır.  
- **DMI yönü söyler**,  
- **ADX ise o yönün güçlü olup olmadığını** belirtir.  

DMI, fiyatın yukarı ya da aşağı yönlü baskın hareketini ölçerken, ADX bu hareketin gücünü derecelendirir.  

- **+DI (Positive Directional Indicator):** Yukarı yönlü baskın hareketin normalize edilmiş değeri  
- **−DI (Negative Directional Indicator):** Aşağı yönlü baskın hareketin normalize edilmiş değeri  
- **ADX:** +DI ile −DI arasındaki farkın pürüzsüzleştirilmesi → trend gücü ölçer  


- DMI ve ADX yorumu:  
  - **+DI > −DI** → Yükseliş eğilimi (bullish)  
  - **−DI > +DI** → Düşüş eğilimi (bearish)  
  - **ADX > 25** → Güçlü trend  
  - **ADX < 20** → Zayıf / yatay piyasa  


DMI hesaplanırken şu adımlar izlenir:  

![DMI Hesaplama](dmi_hesaplama.png)

1. **Yönlü Hareketler (DM):**  
   - +DM = Bugünkü High − Önceki High (pozitifse, aksi halde 0)  
   - −DM = Önceki Low − Bugünkü Low (pozitifse, aksi halde 0)  

2. **True Range (TR):**  
   - TR = max(High − Low, |High − Önceki Close|, |Low − Önceki Close|)  

3. **Pürüzsüzleştirme (Wilder yöntemiyle):**  
   - +DM, −DM ve TR periyot boyunca pürüzsüzleştirilir  

4. **Directional Indicators:**  
   - +DI = 100 × (Smoothed +DM / Smoothed TR)  
   - −DI = 100 × (Smoothed −DM / Smoothed TR)  

5. **ADX:**  
   - ADX = DX’in (100 × |+DI − −DI| / (+DI + −DI)) periyot boyunca pürüzsüzleştirilmesi  


### DMI Algoritması – Sözde Kod

```pseudo
Başla DMI_Hesaplama

Girdi: FiyatlarListesi, Periyot

1. Her gün için:
   a. +DM = max(BugünküHigh - ÖncekiHigh, 0)
   b. -DM = max(ÖncekiLow - BugünküLow, 0)
   c. TR = max(High - Low, |High - ÖncekiClose|, |Low - ÖncekiClose|)

2. +DM, -DM ve TR değerlerini Periyot boyunca Wilder smoothing ile pürüzsüzleştir

3. +DI = 100 × (Smoothed +DM / Smoothed TR)
4. -DI = 100 × (Smoothed -DM / Smoothed TR)

5. DX = 100 × (|+DI - -DI| / (+DI + -DI))
6. ADX = DX’in Periyot boyunca pürüzsüzleştirilmesi

Çıktı: PlusDI, MinusDI, ADX

Bitir
```

Bu algoritmayı kullanarak DMI indiktaörünün nasıl yorumlandığını inceleyelim.

### DMI Nasıl Yorumlanır?

* Trend Yönü:
  * +DI > −DI → Yukarı yönlü baskın → yükseliş sinyali
  * −DI > +DI → Aşağı yönlü baskın → düşüş sinyali

* Trend Gücü (ADX ile birlikte):
  * ADX > 25 → Güçlü trend
  * ADX < 20 → Zayıf / yatay piyasa

* Al-Sat Sinyalleri:
  * Al: +DI, −DI’yi yukarı keser ve ADX yükseliyorsa
  * Sat: −DI, +DI’yi yukarı keser ve ADX yükseliyorsa
  * Tut: ADX düşük veya DI çizgileri iç içeyse

### Grafik Üzerinde Yorumlama

Aşağıdaki GOOGL sembolünün 1 günlük bazdaki yıllık fiyat grafiğinin DMI ve ADX ile olan uyumu gösterilmiştir. DMI grafiğindeki mavi çizgi +DI, kırmızı çizgi -DI'ı ve sarı çizgi ise ADX'i simgelemektedir.

![TradingView GOOGL DMI](tv_googl_dmi.png)

Yukarıdaki grafiğin üzerine çizilen sonuncu trendin başlangıç tarihinde TradeBot'a gönderilen sorgunun çıktısı aşağıdaki gibi olmaktadır. Grafikte de görüldüğü üzere bariz bir düşüş yaşanmıştır ve botun teknik analiz katmanı tarafından Sat sinyali gönderilmiştir. 

````json
{
  "symbol": "GOOGL",
  "indicator": "DMI",
  "date": "2025-09-17T21:00:00Z",
  "values": {
    "plusDIValue": 43.84138316731136,
    "adxValue": 52.74342778309579,
    "minusDIValue": 10.887768379641209
  }
}
````

> Özetle: DMI, +DI ve −DI ile trendin yönünü, ADX ile trendin gücünü gösterir. Her gösterge gibi DMI da geçmişe dayanır; geçmiş başarı gelecek başarıyı garanti etmez, yönü söyler ama tek başına bir giriş/çıkış stratejisi olmamalıdır. Yanlış tetiklemeleri azaltmak için ADX gibi güç ölçerlerle filtrelemek gerekir. 

---

#### Dipnot

> Bu rapor, geliştirilmekte olan TradeBot projesinin teknik analiz katmanını açıklayan ve özetleyen bir araştırma raporudur. Bu sadece bir araştırma raporu olduğundan ve raporda yazan hiçbir şey bir uzman tarafından yazılmamış olduğundan ötürü yatırım tavsiyesi niteliğinde değildir.

---

## Kaynakça

[1] - [Türkiye Finans - Finansal Analiz Türleri](https://www.turkiyefinans.com.tr/tr-tr/blog/sayfalar/finansal-analiz-turleri-nelerdir.aspx)

[2] - [TradingView](https://www.tradingview.com/chart/GgCX5Z7V/?symbol=GOOGL) 

[3] - [VantageMarkets - 16 Candlestick Charts Traders Need to Know](https://www.vantagemarkets.com/academy/16-candlestick-charts-traders-need-to-know/)

[4] - [AlphaVantage](https://www.alphavantage.co/documentation/)

[5] - [YerliForex - Teknik Analiz](https://yerliforex.com/forex-egitim/teknik-analiz/)

[6] - [Changelly Cheat Sheet](https://changelly.com/blog/chart-patterns-cheat-sheet/)

[7] - Technical Analysis for Dummies, 2004, Rockefeller B

[8] - [Investopedia - MFI](https://www.investopedia.com/terms/m/mfi.asp)