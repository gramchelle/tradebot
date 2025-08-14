import ssl
ssl._create_default_https_context = ssl._create_unverified_context

import yfinance as yf

data = yf.download("AAPL", start="2020-01-01", end="2025-01-01")
print(data.head())
