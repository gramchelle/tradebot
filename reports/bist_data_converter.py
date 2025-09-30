import os
import pandas

# BIST100 ISMEN Historical Data converter pipeline

path = r"C:\\Users\\Özlem Nur\\Desktop\\bist100\\"
output_dir = r"C:\Users\Özlem Nur\Desktop\bist100\converted"
os.makedirs(output_dir, exist_ok=True)

def parse_date_series(series):
    # common formats in the dataset: '15.09.2025', '15-09-2025', '2025-09-15'
    formats = ["%d.%m.%Y", "%d-%m-%Y", "%Y-%m-%d", "%d/%m/%Y"]
    for fmt in formats:
        try:
            return pandas.to_datetime(series, format=fmt)
        except Exception:
            continue
    return pandas.to_datetime(series, dayfirst=True, errors="coerce")


for file in os.listdir(path):
    if not file.lower().endswith(".xlsx"):
        continue

    df = pandas.read_excel(os.path.join(path, file))
    df["symbol"] = file.replace(".xlsx", "").replace("i", "ı").upper()
    print("Processing:", df["symbol"].unique())

    if "Tarih" not in df.columns:
        print(f"Skipping {file}: no 'Tarih' column")
        continue

    df["timestamp"] = parse_date_series(df["Tarih"])
    na_count = df["timestamp"].isna().sum()
    if na_count:
        print(f"Warning: {na_count} rows had unparseable dates in {file}")

    df["timestamp"] = df["timestamp"].dt.normalize()

    converted_df = pandas.DataFrame()
    converted_df[["close", "high", "low", "open", "symbol", "timestamp", "volume"]] = df[["Kapanış(TL)", "Max(TL)", "Min(TL)", "AOF(TL)", "symbol", "timestamp", "Hacim(TL)"]]

    print(converted_df.head())

    # save as CSV named after symbol into requested output folder
    csv_name = f"{df['symbol'].iloc[0]}.csv"
    out_path = os.path.join(output_dir, csv_name)
    converted_df.to_csv(out_path, index=False, encoding="utf-8")
    print("Saved:", out_path)
