package com.goquant.quantplatform.controller.algo;

import com.goquant.quantplatform.entity.BarData;
import com.goquant.quantplatform.entity.OrderData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MemeAlgo extends BaseAlgo {
    private static final Logger LOG = LoggerFactory.getLogger(MemeAlgo.class);

    @Override
    public void onAlgoStart() {

    }

    @Override
    public void onDayStart() {

    }

    @Override
    public Set<OrderData> onBar(BarData barData) {
        return null;
    }

    @Override
    public void onDayEnd() {

    }

    @Override
    public Set<String> getSymbols() {
        return null;
    }
}
