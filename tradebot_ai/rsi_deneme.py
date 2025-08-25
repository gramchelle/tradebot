import yfinance as yf
import pandas as pd
import matplotlib.pyplot as plt

# Örnek hisse: Apple (AAPL)
data = yf.download("AAPL", start="2024-01-01", end="2024-08-01")

# RSI hesaplama
window = 14
delta = data["Close"].diff()
gain = (delta.where(delta > 0, 0)).rolling(window=window).mean()
loss = (-delta.where(delta < 0, 0)).rolling(window=window).mean()

rs = gain / loss
rsi = 100 - (100 / (1 + rs))

# Grafik çizimi
plt.figure(figsize=(12,6))

# Fiyat grafiği
plt.subplot(2,1,1)
plt.plot(data.index, data["Close"], label="Fiyat (AAPL)")
plt.title("AAPL Fiyat ve RSI")
plt.legend()

# RSI grafiği
plt.subplot(2,1,2)
plt.plot(data.index, rsi, label="RSI", color="orange")
plt.axhline(70, color="red", linestyle="--", alpha=0.7)
plt.axhline(30, color="green", linestyle="--", alpha=0.7)
plt.legend()

plt.show()
