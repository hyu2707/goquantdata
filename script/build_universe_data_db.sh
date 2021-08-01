#!/bin/bash

BASEDIR=`echo "$(cd "$(dirname "$1")"; pwd -P)/$(basename "$1")"`

export PYTHONPATH=${BASEDIR}:$PYTHONPATH

env/bin/python $BASEDIR/script/data/download_universe_data.py --debug
