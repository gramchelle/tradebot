import os
from datetime import timedelta
import pandas as pd
import numpy as np
import yfinance as yf
import talib
import json

# read custom cert if present (keeps compatibility with your environment)
try:
    with open("tradebot_ai\\secrets\\cacert_path.txt") as f:
        cert_path = f.read().strip()
    os.environ["SSL_CERT_FILE"] = cert_path
    os.environ["REQUESTS_CA_BUNDLE"] = cert_path
except Exception:
    pass

START_DATE = "2015-01-01"
END_DATE = "2025-09-04"
DATE_RANGE = pd.date_range(START_DATE, END_DATE, freq="W")  # weekly targets

SYMBOLS = ['GOOGL', 'NVDA', 'NFLX']
INDICATORS = ["rsi", "macd", "bollinger", "ma-cross", "dmi", "mfi", "trendline"]


class ExternalSignal:
    def __init__(self, start=START_DATE, end=END_DATE):
        self.start = pd.to_datetime(start)
        self.end = pd.to_datetime(end)
        self._history_cache = {}

    def _fetch_history(self, symbol):
        # cache one download per symbol for efficiency
        if symbol in self._history_cache:
            return self._history_cache[symbol]
        # fetch full daily history once (auto_adjust True to match TA calculations)
        # Fetch extra history before self.start so early targets have enough lookback
        extra_days = 365  # one year of extra history; adjust if you need more
        history_start = (self.start - pd.Timedelta(days=extra_days)).strftime("%Y-%m-%d")
        df = yf.download(symbol, start=history_start,
                         end=(self.end + pd.Timedelta(days=1)).strftime("%Y-%m-%d"),
                         interval='1d', auto_adjust=True, progress=False)

        # immediate raw debug for problematic symbols
        if symbol in ("GOOGL", "NVDA"):
            try:
                print(f"DEBUG_RAW: {symbol} type={type(df)} shape={getattr(df, 'shape', None)}")
                if hasattr(df, 'columns'):
                    cols = list(df.columns[:10]) if getattr(df, 'shape', (0,))[1] > 10 else list(df.columns)
                    nlevels = getattr(df.columns, 'nlevels', 1)
                    print(f"DEBUG_RAW_COLS_SAMPLE: {symbol} cols_sample={cols} nlevels={nlevels}")
            except Exception as e:
                print(f"DEBUG_RAW_ERR: {symbol} {e}")

        # debug raw download when empty and try fallback
        if df is None or (hasattr(df, 'shape') and df.shape[0] == 0):
            if symbol in ("GOOGL", "NVDA"):
                print(f"DEBUG_RAW_DOWNLOAD_EMPTY: {symbol} df is None or empty, type={type(df)} shape={getattr(df, 'shape', None)}")
            try:
                # fallback to Ticker.history which sometimes behaves better
                t = yf.Ticker(symbol)
                df_fallback = t.history(start=history_start,
                                         end=(self.end + pd.Timedelta(days=1)).strftime("%Y-%m-%d"),
                                         interval='1d', auto_adjust=True)
                if df_fallback is not None and getattr(df_fallback, 'shape', (0,))[0] > 0:
                    df = df_fallback
                    if symbol in ("GOOGL", "NVDA"):
                        print(f"DEBUG_FALLBACK_OK: {symbol} fallback rows={len(df)}")
                else:
                    if symbol in ("GOOGL", "NVDA"):
                        print(f"DEBUG_FALLBACK_EMPTY: {symbol} fallback empty too")
            except Exception as e:
                if symbol in ("GOOGL", "NVDA"):
                    print(f"DEBUG_FALLBACK_ERR: {symbol} {e}")

        # normalize result to a DataFrame
        if df is None:
            df = pd.DataFrame()
        # If yfinance returned a Series (single-column), convert to DataFrame
        if isinstance(df, pd.Series):
            df = df.to_frame()

        # handle MultiIndex columns (when multiple tickers were requested)
        if hasattr(df.columns, "nlevels") and df.columns.nlevels > 1:
            # try to detect which MultiIndex level contains the ticker symbol
            found = False
            for lvl in range(df.columns.nlevels):
                try:
                    vals = set(df.columns.get_level_values(lvl))
                    if symbol in vals:
                        df = df.xs(symbol, axis=1, level=lvl)
                        found = True
                        if symbol in ("GOOGL", "NVDA"):
                            print(f"DEBUG_MULTIIDX: extracted symbol {symbol} using level {lvl}")
                        break
                except Exception:
                    continue
            if not found:
                # fallback: flatten multiindex to single level by joining names
                df.columns = ["_".join(map(str, col)).strip() for col in df.columns.values]
                if symbol in ("GOOGL", "NVDA"):
                    print(f"DEBUG_MULTIIDX: could not find symbol level; flattened columns sample={list(df.columns)[:5]}")

        # normalize index and only drop rows where all available OHLCV are NaN
        if not df.empty:
            df.index = pd.to_datetime(df.index)

            required = ["Open", "High", "Low", "Close", "Volume"]
            available = [c for c in required if c in df.columns]

            if available:
                df = df.dropna(subset=available, how="all")
            else:
                # no expected OHLCV columns present -> return empty DataFrame to signal upstream
                df = pd.DataFrame()

        # debug: print summary for common symbols to diagnose NA results
        if symbol in ("GOOGL", "NVDA"):
            try:
                print(f"DEBUG_FETCH: {symbol} rows={len(df)} cols={list(df.columns)} index_min={None if df.empty else df.index.min()} index_max={None if df.empty else df.index.max()}")
                if not df.empty:
                    print(f"DEBUG_FETCH_TAIL: {symbol} tail close: {df['Close'].dropna().tail(5).to_list()}")
            except Exception as e:
                print(f"DEBUG_FETCH_ERR: {symbol} {e}")

        self._history_cache[symbol] = df
        return df

    def _prepare_arrays(self, df_up_to_date):
        # remove NaNs and return 1-D numpy arrays
        series = df_up_to_date.dropna(subset=["Close"])
        close = series["Close"].astype(float).to_numpy()
        high = series["High"].astype(float).to_numpy() if "High" in series else None
        low = series["Low"].astype(float).to_numpy() if "Low" in series else None
        volume = series["Volume"].astype(float).to_numpy() if "Volume" in series else None
        return close, high, low, volume

    # indicator implementations (operate on 1-D numpy arrays)
    def calculate_rsi(self, close, period=14):
        arr = talib.RSI(close, timeperiod=period)
        if arr.size == 0 or np.isnan(arr[-1]):
            return "NA"
        v = arr[-1]
        return "Sell" if v > 70 else "Buy" if v < 30 else "Hold"

    def calculate_macd(self, close, fast=12, slow=26, signal=9):
        macd, sig, hist = talib.MACD(close, fastperiod=fast, slowperiod=slow, signalperiod=signal)
        if macd.size == 0 or np.isnan(macd[-1]) or np.isnan(sig[-1]):
            return "NA"
        return "Buy" if macd[-1] > sig[-1] else "Sell" if macd[-1] < sig[-1] else "Hold"

    def calculate_bollinger(self, close, timeperiod=20, nbdevup=2, nbdevdn=2):
        upper, middle, lower = talib.BBANDS(close, timeperiod=timeperiod, nbdevup=nbdevup, nbdevdn=nbdevdn)
        if upper.size == 0 or np.isnan(upper[-1]) or np.isnan(lower[-1]):
            return "NA"
        price = close[-1]
        return "Sell" if price > upper[-1] else "Buy" if price < lower[-1] else "Hold"

    def calculate_ma_cross(self, close, short=50, long=100):
        ma_short = talib.EMA(close, timeperiod=short)
        ma_long = talib.EMA(close, timeperiod=long)
        if ma_short.size == 0 or ma_long.size == 0 or np.isnan(ma_short[-1]) or np.isnan(ma_long[-1]):
            return "NA"
        return "Buy" if ma_short[-1] > ma_long[-1] else "Sell" if ma_short[-1] < ma_long[-1] else "Hold"

    def calculate_dmi(self, high, low, close, period=14):
        if high is None or low is None:
            return "NA"
        plus_di = talib.PLUS_DI(high, low, close, timeperiod=period)
        minus_di = talib.MINUS_DI(high, low, close, timeperiod=period)
        if plus_di.size == 0 or minus_di.size == 0 or np.isnan(plus_di[-1]) or np.isnan(minus_di[-1]):
            return "NA"
        return "Buy" if plus_di[-1] > minus_di[-1] else "Sell" if plus_di[-1] < minus_di[-1] else "Hold"

    def calculate_mfi(self, high, low, close, volume, period=14):
        if high is None or low is None or volume is None:
            return "NA"
        mfi = talib.MFI(high, low, close, volume, timeperiod=period)
        if mfi.size == 0 or np.isnan(mfi[-1]):
            return "NA"
        v = mfi[-1]
        return "Sell" if v > 80 else "Buy" if v < 20 else "Hold"

    def calculate_trendline(self, close, window=20):
        if close.size == 0 or np.isnan(close[-1]):
            return "NA"
        mean = np.nanmean(close[-window:]) if close.size >= window else np.nanmean(close)
        if np.isnan(mean):
            return "NA"
        return "Buy" if close[-1] > mean else "Sell" if close[-1] < mean else "Hold"

    def get_signal_for_date(self, symbol, indicator, target_date):
        # target_date can be string or Timestamp
        target = pd.to_datetime(target_date)
        df = self._fetch_history(symbol)
        if df.empty:
            return symbol, indicator, "NA", target_date

        # # use all available daily bars up to the target trading day (inclusive)
        # df_up_to = df[df.index.date <= target.date()]
        # if df_up_to.empty:
        #     # debug: show why empty
        #     if symbol in ("GOOGL", "NVDA"):
        #         print(f"DEBUG_NO_DATA_UP_TO: {symbol} target={target.date()} df_rows={len(df)} index_min={None if df.empty else df.index.min()} index_max={None if df.empty else df.index.max()}")
        #     return symbol, indicator, "NA", target_date

        # close, high, low, volume = self._prepare_arrays(df_up_to)

        # # minimal length requirements to avoid TA-Lib dimension errors
        # min_lengths = {
        #     "rsi": 15, "macd": 35, "bollinger": 21, "ma-cross": 50, "dmi": 15, "mfi": 15, "trendline": 5
        # }
        # min_len = min_lengths.get(indicator, 10)
        # if close.size < min_len:
        #     # debug: print array lengths for investigation on common symbols
        #     if symbol in ("GOOGL", "NVDA"):
        #         print(f"DEBUG_INSUFFICIENT_LEN: {symbol} {indicator} target={target.date()} close_len={close.size} required={min_len}")
        #         try:
        #             print(f"DEBUG close tail: {close[-10:]}" if close.size>0 else "DEBUG close empty")
        #         except Exception:
        #             print("DEBUG close tail: error retrieving tail")
        #     return symbol, indicator, "NA", target_date

        try:
            if indicator == "rsi":
                signal = self.calculate_rsi(close)
            elif indicator == "macd":
                signal = self.calculate_macd(close)
            elif indicator == "bollinger":
                signal = self.calculate_bollinger(close)
            elif indicator == "ma-cross":
                signal = self.calculate_ma_cross(close)
            elif indicator == "dmi":
                signal = self.calculate_dmi(high, low, close)
            elif indicator == "mfi":
                signal = self.calculate_mfi(high, low, close, volume)
            elif indicator == "trendline":
                signal = self.calculate_trendline(close)
            else:
                signal = "NA"
        except Exception as e:
            # keep running; return NA for problematic calculations
            print(f"[!] calc error {symbol} {indicator} {target_date}: {e}")
            signal = "NA"

        return symbol, indicator, signal, target_date

    def generate_report_for_symbol(self, symbol, indicators=INDICATORS):
        report = []
        for i, target in enumerate(DATE_RANGE):
            for ind in indicators:
                sym, indicator, signal, date_val = self.get_signal_for_date(symbol, ind, target)
                report.append({
                    "id": len(report) + 1,
                    "symbol": sym,
                    "indicator": indicator,
                    "signal": signal,
                    "date": pd.to_datetime(date_val).strftime("%Y-%m-%d")
                })
        # write CSV per symbol
        output_dir = os.path.join("tradebot_ai", "reports", "external")
        os.makedirs(output_dir, exist_ok=True)
        out_path = os.path.join(output_dir, f"{symbol}_external_report.csv")
        pd.DataFrame(report).to_csv(out_path, index=False)
        print(f"Report written: {out_path}  (rows: {len(report)})")
        return out_path


if __name__ == "__main__":
    ext = ExternalSignal()
    for s in SYMBOLS:
        ext.generate_report_for_symbol(s, INDICATORS)