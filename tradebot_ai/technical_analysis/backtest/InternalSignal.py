# Bismillahirrahmanirrahim
import pandas as pd
import requests
import os

with open("tradebot_ai\\secrets\\cacert_path.txt") as f:
    cert_path = f.read().strip()

# -- SSL sertifika ayarları --
os.environ["SSL_CERT_FILE"] = cert_path
os.environ["REQUESTS_CA_BUNDLE"] = cert_path

# -- Konfigürasyon --
BASE_URL = "http://localhost:8082/ta"
SYMBOLS = ['GOOGL', 'NVDA', 'NFLX']
INDICATORS = ["rsi", "macd", "bollinger", "ma-cross", "dmi", "mfi", "trendline"]

START_DATE = "2015-01-01"
END_DATE = "2025-09-04"
DATE_RANGE = pd.date_range(START_DATE, END_DATE, freq="W") # Weekly

class InternalSignal:
    tradebot_signals_df = pd.DataFrame(columns=["id", "symbol", "indicator", "signal", "date"])

    # --- Yardımcı Fonksiyonlar ---
    def get_internal_signal(self,symbol, indicator, date):
        """
            Get signals from Spring Boot API
        """
        url = f"{BASE_URL}/{indicator}/{symbol}?date={date}T00:00:00"
        try:
            resp = requests.get(url, timeout=5)
            data = resp.json()
            filtered = {k: data.get(k, "NA") for k in ["signal"]}
            return symbol, indicator, filtered["signal"], date
        except Exception as e:
            print(f"[!] An error occurred while fetching internal signal for {symbol}, {indicator}, {date}: {e}")
            raise e

    def generate_report(self, symbol, indicators):
        print("Processing for symbol:", symbol)
        report = []
        for ind in indicators:
            print(f"Processing indicator: {ind}")
            for i, date in enumerate(DATE_RANGE):
                date_str = date.strftime("%Y-%m-%d")
                try:
                    result = self.get_internal_signal(symbol, ind, date_str)
                    if result is not None:
                        symbol, indicator, signal, date_val = result
                        report.append({
                            "id": i,
                            "symbol": symbol,
                            "indicator": indicator,
                            "signal": signal,
                            "date": date_val
                        })
                    else:
                        print(f"[!] Skipping {symbol}, {ind}, {date_str} due to error in get_internal_signal.")
                except Exception as e:
                    print(f"[!] Skipping {symbol}, {ind}, {date_str} due to exception: {e}")
            df_report = pd.DataFrame(report)
            try:
                output_dir = os.path.join("tradebot_ai", "reports", "internal")
                os.makedirs(output_dir, exist_ok=True)
                output_path = os.path.join(output_dir, f"{symbol}_internal_report.csv")
                df_report.to_csv(output_path, index=False)
                print("Report is created at ", output_path)
            except Exception as e:
                print(f"[!] An error occurred while saving the report for {symbol}: {e}")

    """
    for ind in INDICATORS:
        print(f"Processing indicator: {ind}")
        for i, date in enumerate(DATE_RANGE):
            print(f"Processing date: {date.strftime('%Y-%m-%d')}")
            date_str = DATE_RANGE[i].strftime("%Y-%m-%d")
            internal_result = get_internal_signal(sym, ind, date_str)
            if internal_result is not None:
                symbol, indicator, signal, date_val = internal_result
                new_item = pd.Series({"symbol": symbol, "indicator": indicator, "signal": signal, "date": date_val})
                dynamic_internal_signals.loc[len(dynamic_internal_signals)] = [len(dynamic_internal_signals) + 1, symbol, indicator, signal, date_val]
            else:
                print(f"[!] Skipping {sym}, {ind}, {date_str} due to error in get_internal_signal.")
        # Save to CSV after all rows are added for this indicator
        tradebot_signals_df.to_csv(f"{sym}_{ind}_internal_signals.csv", index=False)
    """

# -- Ana Fonksiyon --
if __name__ == "__main__":
    internal_signal = InternalSignal()
    try:
        for symbol in SYMBOLS:
            internal_signal.generate_report(symbol, INDICATORS)
    except Exception as e:
        print(f"[!] An error occurred in the main execution: {e}")