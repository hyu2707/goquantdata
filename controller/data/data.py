import pandas as pd
import os
import pytz
from datetime import datetime, timezone, timedelta
from util.logger import logger
import shutil
from os import listdir
from os.path import isfile, join

from entity.constants import *
from config.config import TradingConfig

from gateway.bitmex import BitmexGateway
from gateway.s3 import S3Gateway
from gateway.alpaca import AlpacaGateway
from gateway.polygon_gateway import PolygonGateway, PolygonRequestException
from entity.mapper import binance_to_goquant, data_polygon_to_goquant, orderbook_to_orderbook_df, data_alpaca_to_goquant
from util.date import get_datetime_list_between, date_to_milliseconds


STRING_FORMAT = "%Y%m%d"

class GQData(object):

    def __init__(self):
        self.alpaca = AlpacaGateway()
        self.polygon = PolygonGateway()
        self.bitmex = BitmexGateway()
        # self.s3 = S3Gateway()
        self.cfg = TradingConfig()
        self.df_all = None

    def get_data(self,
                 symbols,
                 freq,
                 start_date,
                 end_date=datetime.now(timezone.utc).strftime(STRING_FORMAT),
                 datasource=DATASOURCE_POLYGON,
                 data_type=DATATYPE_TICKER):
        """
        get historical data
        :param symbols: list of string
            list of symbols
        :param freq: string
            sec, min, day, week
        :param start_date: string
            start_date in YYYY-MM-DD format
        :param end_date:
            end_date in YYYY-MM-DD format
        :param datasource: string
            data source
        :param data_type: string
            ticker or orderbook
        :return: dataframe
            dataframe contains symbols historical data
        """
        if datasource not in VALID_DATASOURCE:
            raise ValueError(
                "datasource {} not in valid list {}".format(
                    datasource, VALID_DATASOURCE))
        if freq not in VALID_FREQ:
            raise ValueError(
                "freq {} not in valid list {}".format(
                    freq, VALID_FREQ))
        if len(symbols) == 0:
            raise ValueError("symbols are empty")
        logger.info("loading data...")

        start_date = datetime.strptime(start_date, STRING_FORMAT)
        end_date = datetime.strptime(end_date, STRING_FORMAT)

        # loading data
        if datasource == DATASOURCE_CACHE:
            df = self._get_prices_cache(freq, start_date, end_date, data_type)
        else:
            # get data from data provider
            df = self._get_prices_remote(symbols, freq, start_date, end_date, datasource, data_type)
            self._save_df(df, freq)
        if df is None or df.shape[0] == 0:
            logger.error("load empty data")
        else:
            logger.info("loaded done. symbol number {}, total rows: {}".format(len(df[DATA_SYMBOL].unique()), df.shape[0]))
        return df

    def get_universe_data(self,
                          datasource):
        if datasource != DATASOURCE_POLYGON:
            raise ValueError("only support polygon universe for now")

        logger.info("loading data...")

        df = self.polygon.get_recent_universe_data()
        logger.info("loaded done. symbol number {}".format(df.shape[0]))
        key = self.get_universe_key()
        self.save_df(df, key)
        return df


    def clean_cache(self):
        if os.path.exists(self.cfg.csv_data_path):
            shutil.rmtree(self.cfg.csv_data_path)

    def _post_process_data(self, data_dict, fill_nan_method, remove_nan_rows):
        data_dict = self._fill_nan(data_dict, fill_nan_method, remove_nan_rows)
        # make sure symbol is not nan
        for symbol in data_dict:
            data_dict[symbol][DATA_SYMBOL] = symbol
        return data_dict

    @staticmethod
    def _fill_nan(data_dict, fill_nan_method=None, remove_nan_rows=False):
        union_index = pd.Index([])
        for symbol in data_dict:
            cur_df = data_dict[symbol]
            union_index = union_index.union(cur_df.index)
        union_index = union_index.sort_values()
        keep_index = None
        ret_dict = {}
        for symbol in data_dict:
            cur_df = data_dict[symbol]
            new_df = cur_df.reindex(union_index)
            if fill_nan_method is not None:
                new_df = new_df.fillna(method=fill_nan_method)
            if remove_nan_rows:
                keep_df = new_df.dropna(how='any')
                if keep_index is None:
                    keep_index = keep_df.index
                else:
                    keep_index = keep_index.intersection(keep_df.index)
            ret_dict[symbol] = new_df.copy()
        if remove_nan_rows and keep_index is not None:
            for symbol in ret_dict:
                ret_dict[symbol] = ret_dict[symbol].loc[keep_index, :]

        return ret_dict

    def _get_prices_cache(self, freq, start_date, end_date, data_type):
        dt_list = pd.date_range(start_date, end_date - timedelta(days=1), freq='d')
        df = None
        for dt in dt_list:
            key = self.get_data_key(dt, freq, data_type)
            cur_df = self.load_df(key)
            if cur_df is None:
                continue
            if df is None:
                df = cur_df
            else:
                df = pd.concat([df, cur_df], sort=False)
        return df

    def _get_prices_remote(self, symbols, freq, start_date, end_date, datasource, data_type,
                           fill_nan_method=None,
                           remove_nan_rows=True):
        df_dict = {}
        if data_type == DATATYPE_TICKER:
            if datasource == DATASOURCE_POLYGON:
                df_dict = self._polygon_get_prices(
                    symbols, freq, start_date, end_date)
            elif datasource == DATASOURCE_ALPACA:
                df_dict = self._alpaca_get_prices(
                    symbols, freq, start_date, end_date)
            elif datasource == DATASOURCE_BINANCE:
                df_dict = self._binance_get_prices(
                    symbols=symbols,
                    freq=freq,
                    start_datetime=start_date,
                    end_datetime=end_date)
            else:
                logger.error("unsupported datasource: {}".format(datasource))
        elif data_type == DATATYPE_ORDERBOOK:
            if datasource == DATASOURCE_BITMEX:
                df_dict = self._bitmex_get_orderbook(symbols=symbols,
                                                     freq=freq,
                                                     start_datetime=start_date,
                                                     end_datetime=end_date)
        else:
            logger.error("unsupported data_type: {}".format(data_type))

        # post-process data, such as data clean, fill nan
        df_dict = self._post_process_data(df_dict, fill_nan_method=fill_nan_method, remove_nan_rows=remove_nan_rows)

        # merge dict to df
        df = None
        for symbol in df_dict:
            if df is None:
                df = df_dict[symbol]
            else:
                df = pd.concat([df, df_dict[symbol]], sort=False)
        df['dt'] = df.index.date
        return df

    def _save_df(self, df, freq):
        for dt in df['dt'].unique().tolist():
            cur_df = df.loc[df['dt'] == dt].drop(['dt'], axis=1)
            key = self.get_data_key(dt, freq, DATATYPE_TICKER)
            self.save_df(cur_df, key)
        return

    def check_data_key(self, data_key):
        snapshot_path = self.get_data_snapshot_path(data_key)
        return os.path.exists(snapshot_path) and len(os.listdir(snapshot_path)) > 0

    def get_data_key(self, dt, freq, data_type=DATATYPE_TICKER):
        dt_str = dt.strftime(STRING_FORMAT)
        key = DATA_FILE_FMT.format(type=data_type, freq=freq, dt=dt_str)
        return key

    def get_universe_key(self):
        return UNIVERSE_DATA_FILE_FMT.format(type=DATATYPE_UNIVERSE)

    def save_df(self, df, key):
        snapshot_path = self.get_data_snapshot_path(key)
        if not os.path.exists(snapshot_path):
            os.makedirs(snapshot_path)

        # save df into the dir
        filepath = "{}/snapshot.dat".format(snapshot_path)
        df = df.tz_convert(None).reset_index().rename(columns={'index': DATA_DATETIME}).to_csv(filepath, index=False)
        logger.info("saved data: {}".format(filepath))

    def load_df(self, data_key):
        if not self.check_data_key(data_key):
            logger.warning("missing data key in cache, dt: {}".format(data_key))
            return None

        snapshot_path = self.get_data_snapshot_path(data_key)
        data_files = [join(snapshot_path, f) for f in os.listdir(snapshot_path) if isfile(join(snapshot_path, f))]
        df = None
        for filepath in data_files:
            logger.debug("loading data from file: {}".format(filepath))
            cur_df = pd.read_csv(filepath, index_col=False)
            if df is None:
                df = cur_df
            else:
                df = pd.concat([df, cur_df], sort=False)
        if df is not None and df.shape[0] > 0:
            df[DATA_DATETIME] = pd.to_datetime(df[DATA_DATETIME])
            df = df.drop_duplicates()
            df.set_index(DATA_DATETIME, inplace=True)
        return df

    def get_data_snapshot_path(self, data_key):
        return "{}/{}".format(self.cfg.csv_data_path, data_key)

    def _alpaca_get_prices(self, symbols, freq, start_datetime, end_datetime):
        df_dict = {}
        for symbol in symbols:
            try:
                cur_df = self.alpaca.get_historical_data(
                    symbol=symbol,
                    freq=freq,
                    start_date_str=start_datetime.strftime(
                        PolygonGateway.DATE_FMT),
                    end_date_str=end_datetime.strftime(
                        PolygonGateway.DATE_FMT))
            except Exception as err:
                logger.error(
                    "alpaca get error when load data, err:{}".format(err))
                continue
            gq_cur_df = data_alpaca_to_goquant(cur_df, symbol)
            df_dict[symbol] = gq_cur_df
        return df_dict

    def _polygon_get_prices(self, symbols, freq, start_datetime, end_datetime):
        df_dict = {}
        for symbol in symbols:
            try:
                cur_df = self.polygon.get_historical_data(
                    symbol=symbol,
                    freq=freq,
                    start_date_str=start_datetime.strftime(
                        PolygonGateway.DATE_FMT),
                    end_date_str=end_datetime.strftime(
                        PolygonGateway.DATE_FMT),
                    unadjusted=False)
            except PolygonRequestException as err:
                logger.warning(
                    "polygon get error when load data, err:{}".format(err))
                continue
            gq_cur_df = data_polygon_to_goquant(cur_df)
            df_dict[symbol] = gq_cur_df
        return df_dict

    def _binance_get_prices(self, symbols, freq, start_datetime, end_datetime):
        out_dict = {}
        for symbol in symbols:
            data_df = self.binance.get_historical_klines(
                symbol=symbol,
                freq=freq,
                start_datetime=start_datetime,
                end_datetime=end_datetime
            )
            self._check_data(data_df)

            cur_df = binance_to_goquant(
                symbol=symbol,
                in_data=data_df)
            out_dict[symbol] = cur_df
        return out_dict

    def _bitmex_get_orderbook(self, symbols, freq, start_datetime, end_datetime):
        out_dict = {}
        datetime_list = get_datetime_list_between(start_datetime, end_datetime, freq)
        for symbol in symbols:
            cur_df = None
            for cur_datetime in datetime_list:
                logger.debug("s3 loading data at: {}, symbol: {}".format(cur_datetime, symbol))
                ts = date_to_milliseconds(cur_datetime)
                row_orderbook = self.s3.get_orderbook(symbol=symbol, ts=ts)
                if row_orderbook is None:
                    continue
                orderbook_df = orderbook_to_orderbook_df(row_orderbook)
                if cur_df is None:
                    cur_df = orderbook_df
                else:
                    cur_df = cur_df.append(orderbook_df, ignore_index=False, sort=False)
            out_dict[symbol] = cur_df
        return out_dict

    def _check_data(self, data_df):
        if data_df.empty:
            err = ValueError("get_prices return empty data")
            logger.error(err)
            raise err
        else:
            logger.debug("get number of data shape: " + str(data_df.shape))
