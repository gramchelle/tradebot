# Backtesting

Backtest, yani geriye dönük test, borsada işlem gören neredeyse tüm varlık türleri için yapılan analizlerin geçmiş fiyat verileriyle test edilerek kullanılan stratejinin metriklerini hesaplama yöntemine verilen addır.

Tarihsel verilerle geçmişe yönelik analizler yaparak bir stratejinin uygulanabilirliğini değerlendirir. [1]

Amaç teknik analiz ile aynıdır:
1. Geçmiş verileri kullanarak geleceğe yönelik tahminler üretmek
2. Stratejinizin iyi çalışıp çalışmadığını anlamak
3. Stratejinizi geliştirmeye yardım etmek

> Backtest, bir trader'ın, herhangi bir sermayeyi riske atmadan önce potansiyel risk ve karlılığı analiz etmek için kullanılan tarihsel verileri analiz etmektedir.

Yüzde getiri, ortalama kar, kaç işlem yapıldı (hacim), kaç işlem karla/zararla kapandı gibi metrikler ölçülür. Genellikle yüzde getiri dikkate alınır.

> Backtest, piyasada herhangi bir enstrümanla işlem yapmadan önce yapılması gereken çok kritik bir adımdır. Backtestsiz işleme girilen tüm stratejiler "gözü kapalı" bir şekilde riske atılmayı temsil eder.

## Backtest'in yararları

* Olumlu sonuçlar veren ve iyi yönetilen bir backtest, yatırımcıya stratejisinin temelde sağlam olduğu ve gerçekte uygulandığında kar elde edebileceği konusunda güvence sağlar.

* Ayrıca iyi yönetilen bir backtest, istenilen sonuca ulaşılamadığı takdirde yatırımcının stratejiyi değiştirmesinin veya ilgili sistemi uygulamayı reddetmesinin önünü açar ve muhtemelen bir zararı daha başlamadan önlemiş olur.

* Robot olarak geliştirilen otomatik sistemlerde, algoritmaların geliştirilmesinde **en önemli** adımdır.

## Terminoloji

* Strateji: Belirli kurallar ve yöntemlerle alım-satım kararlarını yöneten yatırım planı. Yatırımcılar kendi stratejilerini oluşturarak kullanmak istedikleri piyasa üzerinde backtest yaparak stratejilerini geçmişe yönelik test edip, güvenilirliğini ölçerler.

https://gedik.com/yazilar/yatirim/en-iyi-portfoy-stratejileri-nelerdir

* Trading Capital: Alım-satım işlemlerini yapmak için ayrılan sermayeye denir. Kullanılabilir nakit veya marjin.

* Enter/Exit Points: Bir pozisyona giriş ve çıkış fiyat seviyeleri; nereden alıp satılacağını belirler.

* Pozisyon: bir yatırımcının bir finansal varlıkta sahip olduğu açık işlemi ifade eder—bu işlem bir alış (Long) veya satış (Short) şeklinde olabilir. Her işlem, bir pozisyon açılmasıyla başlar ve bu, riskin, kârlılığın ve sermaye yönetiminin belirlenmesinde önemli bir rol oynar. [3]
    * Long pozisyon: Hisseyi düşükten alıp fiyatın yükselmesinden kazanç sağlamak.
    * Short pozisyon: Hisseyi yüksekten ödünç alıp, fiyat düşüşünde satarak kazanç sağlamak. Açığa satış (short selling) olarak da bilinir.

* Take-profit (Kar Realizasyonu): Pozisyon hedef fiyatına ulaştığında kazancın fiilen realize edilmesi, karı güvence altına almak için kullanılır.

* Stop-loss: Pozisyon belirlenen zarar seviyesine ulaştığında otomatik kapanan emir, sermâyeyi korur.
    * Trailing stop: Pozisyon fiyatına göre dinamik olarak hareket eden stop-loss, zararı sınırlamak ve karı korumak için kullanılır.

* Lot/Share
    * Share, bir şirketin sahipliğini temsil eden tek bir hisse senedi birimi. 
    * Lot, genellikle borsada işlemlerin standart birim olarak gruplanması için kullanılır. Bazı piyasalarda hisse alım-satımı minimum lot (örneğin 100 hisse) üzerinden yapılır.

* Emir (Order): Borsada alım veya satım yapmak için verilen talimattır. Yani bireysel yatırımcıların, bir menkul varlığı mevcut fiyattan satın alması veya satması için broker'ına (aracı) ilettiği komuttur.
    * Alım Emri (Buy Order): Belirli bir hisse veya varlığı almak için verilen emir.
    * Satım Emri (Sell Order): Sahip olunan hisseyi veya varlığı satmak için verilen emir.
    * Emir Türleri:
        * Market Order (Piyasa Emri): Anında mevcut fiyat üzerinden alım/satım yapılır.
        * Limit Order (Limitli Emir): Belirli bir fiyat seviyesine ulaşınca alım/satım yapılır.
        * Stop Order (Stop Emri): Fiyat belirli seviyeye gelince tetiklenen emir (örn. stop-loss veya take-profit).

* Trading range:

* Paper Trade: Trading platformları yaygınlaşmadan önce traderların gerçek sermaye kullanmadan, kağıt üzerinde trading yaparak gerekli temel noktaları öğrenmesi için yaptığı öğrenme türüdür. Trader tüm pozisyon, portfolyo ve kar/zararlarını not ederek stratejisini takip eder. Bir tür kağıt üzerinde trading simülasyonudur.
    * Yeni oluşturulan bir stratejiyi uygulamadan önce test etme fırsatı sağlar.
    * Yeni başlayanlara platform hakimiyeti ve trading incelikleri sağlar.
    * Gerçek pazar koşullarındaki duyguları yansıtmayabilir.

![Paper Trading](image-1.png) [5]

* Slippage (Fiyat kayması): Verdilen emir ile emir gerçekleştiğinde oluşan fiyat farkıdır. Yani, istenilen fiyattan işlem olmayıp, biraz daha yüksek veya düşük fiyattan işlem gerçekleşmesi durumudur. Piyasanın hızlı hareket etmesi, düşük likidite gibi nedenlerden kaynaklanır.

* Likidite: Bir varlığın piyasada kolayca alınıp satılabilme derecesidir. 
    * Yüksek Likidite = Varlık kolayca alınıp satılabilir ve fiyat değişimi çok küçük olur.
        * Örneğin BİST'teki büyük şirket hisselerinin (ASELS, ISCTR) istenilen miktarda hızlıca alınıp satılabilmesi gibi.
    * Düşük Likidite = Varlık zor satılır veya alıcı bulmak zaman alır. Fiyatlarda büyük dalgalanmalar olabilir.
        * Örneğin çok az işlem gören küçük şirket hisseleri veya bazı kripto paralar.

* Volatilite: Fiyatların belirli bir dönemde ne kadar hızlı ve büyük değiştiğidir. Yüksek volatilite, risk ve fırsat yüksek anlamına gelir.

![Volatilite](image.png) [4]

* Hacim: Belirli bir zaman diliminde alınıp satılan toplam varlık miktarı; işlem yoğunluğunu gösterir.

# Strateji

Strateji, veya yatırım stratejisi, yatırım performansının maksimize edilmesi ve risklerin yönetilmesi amacıyla risk iştahı, yatırımcı profili, finansal beklentiler ve zaman ufku başta olmak üzere çeşitli faktörle dayalı olarak sermayenin farklı finansal araçlara tahsisi için planlanan yol haritasıdır.

### Strateji Belirleme Adımları (kabaca)

1. Hedef Belirleme
    * Day, Intraday, Swing veya Position trader mısınız?
    * Risk toleransı ve sermaye

2. Piyasa ve Varlık Seçimi (Kritik)
    * Varlık türü: Hisse, Forex, kripto?
    * Varlıkta aranan likidite ve volatilite seviyesi

3. Kullanılacak indikatörler ve sinyal stratejileri
    * Indikatör seçerek sinyal yöntemleri oluşturma

4. Entry/Exit kuralları
    * Long ve short için net kurallar belirlenmeli
    * Stop-loss ve take-profit için seviyeler belirlenmeli

5. Risk yönetimi
    * Pozisyon başına risk %1-2 gibi küçük bir oran
    * Maksimum günlük/haftalık kayıp limiti (trading türüne göre)

6. Backtest ve Optimizasyon
    * Tarihsel verilerle strateji test edilmeli (overfittinge dikkat edilmeli)
    * Farklı parametrelerle optimizasyon yapılmalı (grid search, bayesian vb.)

7. Demo / Paper Trading
    * Gerçek parayı riske atmadan önce demo hesapta test edilmeli (tercihen), tradebot bunun için uygun

8. Sürekli gözlem ve revizyon
    * piyasaya göre strateji revize edilmelidir, tüm piyasalar aynı özelliklere sahip değildir ve aynı hassasiyeti göstermez
    * stratejiye mümkün olduğunca duygusal karar eklemeyin (discretion)


### Sample Strategy 1: Bollinger Bands + RSI

https://www.investopedia.com/ask/answers/121014/how-do-i-create-trading-strategy-bollinger-bands-and-relative-strength-indicator-rsi.asp 



## Backtest Nasıl Uygulanır

 ! [Backtesting Flowchart](tradebot_backtest.png)

 > *"To be successful, you need to treat trading as a business and stocks as your business inventory."* [2]

[1] a Para haber kanalı "Backtest nedir?"

[2] Trading for Dummies

[3] https://tradingfinder.com/tr/education/forex/what-is-trading-position/

[4] https://tradeciety.com/bollinger-bands-explained-step-by-step 

[5] https://www.investopedia.com/terms/p/papertrade.asp 