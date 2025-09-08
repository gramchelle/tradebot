# ...existing code...
import os
import glob
import pandas as pd

symbol = ["GOOGL"]
INDICATORS = ["rsi", "macd", "bollinger", "ma-cross", "dmi", "mfi", "trendline"]

internal_signals_file_dir = f'C:\\Users\\Özlem Nur\\Desktop\\tradebot_backend\\tradebot_ai\\reports\\internal\\{symbol}_internal_report.csv'
external_signals_file_dir = f'C:\\Users\\Özlem Nur\\Desktop\\tradebot_backend\\tradebot_ai\\reports\\external\\{symbol}_external_report.csv'

class CalculateAccuracy:
    def __init__(self, internal_dir=internal_signals_file_dir, external_dir=external_signals_file_dir):
        self.internal_dir = internal_dir
        self.external_dir = external_dir
        self.actual = []
        self.predicted = []

    def read_reports_from_dir(self, directory):
        """
        Read all CSV files in `directory` and return dict {basename: DataFrame}.
        Attempts to parse a 'date' column if present.
        """
        files = sorted(glob.glob(os.path.join(directory, "*.csv")))
        dfs = {}
        for f in files:
            name = os.path.splitext(os.path.basename(f))[0]
            try:
                df = pd.read_csv(f, parse_dates=["date"])
            except Exception:
                df = pd.read_csv(f)
            dfs[name] = df
        return dfs

    def concat_reports(self, directory, source_col="_source"):
        """
        Read and concatenate all CSVs in `directory`. Adds a source filename column.
        """
        parts = []
        for name, df in self.read_reports_from_dir(directory).items():
            df2 = df.copy()
            df2[source_col] = name
            parts.append(df2)
        if parts:
            return pd.concat(parts, ignore_index=True)
        return pd.DataFrame(columns=["id", "symbol", "indicator", "signal", "date"])

    def get_accuracy(self):
        if len(self.actual) != len(self.predicted):
            raise ValueError("Actual and predicted arrays must have the same length")
        correct_predictions = sum(a == p for a, p in zip(self.actual, self.predicted))
        return correct_predictions / len(self.actual) if self.actual else 0

    def to_df(self):
        internal_df = self.concat_reports(self.internal_dir)
        external_df = self.concat_reports(self.external_dir)
        return internal_df, external_df


if __name__ == "__main__":
    ca = CalculateAccuracy()
    internal_df, external_df = ca.to_df()
    print("Internal DataFrame shape:", internal_df)
    print("External DataFrame shape:", external_df)