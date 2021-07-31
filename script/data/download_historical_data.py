import argparse
import csv
from datetime import datetime, timezone

from controller.data.data import GQData, DATATYPE_TICKER, DATASOURCE_POLYGON

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Download data')
    parser.add_argument('--source', type=str, default=DATASOURCE_POLYGON,
                        help='download data source: alpaca, s3, bitmex')
    parser.add_argument('--startdate', type=str, help='startdate in YYYYMMDD format')
    parser.add_argument('--enddate', type=str, help='enddate in YYYYMMDD format')
    parser.add_argument('--freq', type=str, help='frequency: day, minute')
    parser.add_argument('--universe_file', type=str, help='universe file path')

    args = parser.parse_args()
    print("input args: {}".format(args))

    universe = []
    with open(args.universe_file, 'r') as f:
        reader = csv.reader(f, delimiter=',')
        universe = list(reader)
    if len(universe) != 1:
        raise Exception("universe file should contain 1 line, current results: {}".format(universe))
    universe = universe[0]
    print("universe: {}".format(universe))

    gqdata = GQData()
    df_dict = gqdata.get_data(universe,
                              args.freq,
                              args.startdate,
                              end_date=args.enddate,
                              datasource=args.source,
                              use_cache=False,
                              dict_output=False,
                              fill_nan_method=None,
                              remove_nan_rows=True,
                              data_type=DATATYPE_TICKER)

    print("done, loaded {} symbols: {}".format(len(df_dict), df_dict.keys()))
