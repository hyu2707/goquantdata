package com.goquant.quantplatform.controller.algo;

import com.goquant.quantplatform.entity.BarData;
import com.goquant.quantplatform.entity.OrderData;
import com.goquant.quantplatform.gateway.data.BaseDataClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

abstract public class BaseAlgo {
    private static final Logger LOG = LoggerFactory.getLogger(BaseAlgo.class);

    protected BaseDataClient dataClient;

    public abstract void onAlgoStart();

    public abstract void onDayStart();

    public abstract Set<OrderData> onBar(BarData barData);

    public abstract void onDayEnd();

    public abstract Set<String> getSymbols();
}
