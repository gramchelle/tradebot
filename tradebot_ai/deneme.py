import pandas as pd
from alpha_vantage.timeseries import TimeSeries

#df = pd.read_csv('data/sp500_stocks.csv')
#print(df[df["Symbol"] == "TSLA"].head())

api_key = "DCWD1DBDDS6Y2I11" 
ts = TimeSeries(key=api_key, output_format="pandas")

data, meta_data = ts.get_daily(symbol='TSLA', outputsize='full')
print(data.head())

from alpha_vantage.techindicators import TechIndicators

ti = TechIndicators(key=api_key, output_format='pandas')

# SMA hesapla, 20 günlük
sma, meta_data = ti.get_sma(symbol='TSLA', interval='daily', time_period=20, series_type='close')
print("SMA", sma.head())

rsi, meta_data = ti.get_rsi(symbol='TSLA', interval='daily', time_period=14, series_type='close')
print("RSI", rsi.head())

macd, meta_data = ti.get_macd(symbol='TSLA', interval='daily', series_type='close', fastperiod=12, slowperiod=26, signalperiod=9)
print("MACD", macd.head()) 