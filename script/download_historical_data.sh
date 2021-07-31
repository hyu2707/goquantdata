#!/bin/bash

BASEDIR=`echo "$(cd "$(dirname "$1")"; pwd -P)/$(basename "$1")"`

export PYTHONPATH=${BASEDIR}:$PYTHONPATH

env/bin/python $BASEDIR/script/data/download_historical_data.py \
	--source polygon \
	--startdate 20210720 \
	--enddate 20210722 \
	--freq day \
	--universe_file $BASEDIR/script/data/universe.csv
