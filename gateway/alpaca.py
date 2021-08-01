"""
official api: https://github.com/alpacahq/alpaca-trade-api-python/
alpaca account can use polygon.io account
https://polygon.io/docs/#getting-started
"""
import logging
from deprecated.sphinx import deprecated

import alpaca_trade_api as tradeapi

from config.config import TradingConfig
from util.date import datestr_to_datetime
from util.logger import logger


class AlpacaGateway(object):
    DATE_FMT = "%Y-%m-%d"

    def __init__(self):
        self.cfg = TradingConfig()
        self.api_id = self.cfg.alpaca_id
        self.api_key = self.cfg.alpaca_key
        self.api = tradeapi.REST(
            key_id=self.cfg.alpaca_id,
            secret_key=self.cfg.alpaca_key,
            base_url=self.cfg.alpaca_url,
            api_version='v2')

    def get_historical_data(self, symbol, freq, start_date_str, end_date_str):
        # check input format
        datestr_to_datetime(start_date_str, self.DATE_FMT)
        datestr_to_datetime(end_date_str, self.DATE_FMT)

        res_data = self.api.get_aggs(symbol, 1, freq, start_date_str, end_date_str)

        if res_data is None:
            raise Exception("alpaca get empty historical results")
        res_df = res_data.df
        return res_df

    @deprecated(
        reason="alpaca data function has data limit, please use polygon data interface", version=1.0)
    def get_prices(self, symbols, freq='day', length=50):
        def get_barset(symbols):
            return self.api.get_barset(
                symbols,
                freq,
                limit=length
            )

        # The maximum number of symbols we can request at once is 200.
        barset = None
        idx = 0
        while idx <= len(symbols) - 1:
            if barset is None:
                barset = get_barset(symbols[idx:idx + 200])
            else:
                barset.update(get_barset(symbols[idx:idx + 200]))
            idx += 200

        return barset.df
