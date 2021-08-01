import logging
import argparse
import csv
from datetime import datetime, timezone

from controller.data.data import GQData, DATATYPE_TICKER, DATASOURCE_POLYGON

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Download data')
    parser.add_argument("--debug", action='store_true', help="Set to debug mode. Example --debug'")

    args = parser.parse_args()
    print("input args: {}".format(args))

    if args.debug is not None:
        logging.basicConfig(level=logging.DEBUG)
        logger = logging.getLogger(__name__)
        logger.debug('Debug mode on')

    gqdata = GQData()
    df = gqdata.get_universe_data(datasource=DATASOURCE_POLYGON)
