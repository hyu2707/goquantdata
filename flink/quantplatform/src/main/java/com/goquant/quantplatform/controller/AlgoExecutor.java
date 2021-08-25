package com.goquant.quantplatform.controller;

import com.goquant.quantplatform.controller.algo.BaseAlgo;
import com.goquant.quantplatform.controller.algo.MemeAlgo;
import com.goquant.quantplatform.gateway.transaction.BaseTransactionClient;
import com.goquant.quantplatform.entity.BarData;
import com.goquant.quantplatform.entity.OrderData;
import com.goquant.quantplatform.gateway.data.BaseDataClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AlgoExecutor {
    private static final List<BaseAlgo> algoList = new ArrayList<>();

    public AlgoExecutor() {
        algoList.add(new MemeAlgo());
    }

    public void executeAlgo(Instant ts, BaseDataClient dataClient, BaseTransactionClient transactionClient) {
        for (BaseAlgo algo : algoList) {
            algo.onAlgoStart();
        }

        for (BaseAlgo algo : algoList) {
            if (isTsOnDayStart(ts, algo)) {
                algo.onDayStart();
            }

            BarData barData = dataClient.getBarData(ts, algo.getSymbols());
            Set<OrderData> orderDataSet = algo.onBar(barData);
            transactionClient.SubmitOrders(orderDataSet);

            if (isTsOnDayEnd(ts, algo)) {
                algo.onDayEnd();
            }
        }
    }

    private boolean isTsOnDayStart(Instant ts, BaseAlgo algo) {
        return false;
    }

    private boolean isTsOnDayEnd(Instant ts, BaseAlgo algo) {
        return false;
    }

}
