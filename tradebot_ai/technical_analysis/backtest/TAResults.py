import os
from datetime import timedelta
import pandas as pd
import numpy as np
import yfinance as yf
import talib
import json
from ExternalSignal import ExternalSignal

try:
    with open("tradebot_ai\\secrets\\cacert_path.txt") as f:
        cert_path = f.read().strip()
    os.environ["SSL_CERT_FILE"] = cert_path
    os.environ["REQUESTS_CA_BUNDLE"] = cert_path
except Exception:
    pass

INDICATORS = ["rsi", "macd", "bollinger", "ma-cross", "dmi", "mfi", "trendline"]

def get_signal(symbol, date):
    external_signal = ExternalSignal()
    target_date = pd.to_datetime(date)
    rows = []
    for indicator in INDICATORS:
        # call with positional args matching ExternalSignal.get_signal_for_date(symbol, indicator, target_date)
        _, _, signal, _ = external_signal.get_signal_for_date(symbol, indicator, target_date)
        rows.append({"symbol": symbol, "indicator": indicator, "signal": signal, "date": target_date.strftime("%Y-%m-%d")})

    df = pd.DataFrame(rows, columns=["symbol", "indicator", "signal", "date"])
    # out_dir = os.path.join("tradebot_ai", "technical_analysis", "backtest")
    # os.makedirs(out_dir, exist_ok=True)
    # safe_date = target_date.strftime("%Y-%m-%d")
    # out_path = os.path.join(out_dir, f"{symbol}_{safe_date}_signal.csv")
    # df.to_csv(out_path, index=False)
    return df

if __name__ == "__main__":
    symbol = "GOOGL"
    date = "2024-06-18"
    df = get_signal(symbol, date)
    print(df)