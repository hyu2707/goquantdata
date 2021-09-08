package com.goquant.quantplatform.controller;

import com.goquant.quantplatform.common.GoquantConfig;
import com.goquant.quantplatform.common.Util;
import com.goquant.quantplatform.gateway.transaction.BacktestTransactionClient;
import com.goquant.quantplatform.gateway.data.CsvDataClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Instant ts = Util.stringDayToInstant("2021-07-20");

        GoquantConfig cfg;
        try {
            cfg = new GoquantConfig("backtest_dev");
        } catch (IOException e) {
            LOG.error("can't load config file");
            return;
        }


        CsvDataClient csvDataClient = new CsvDataClient(cfg);
        csvDataClient.start();

        BacktestTransactionClient backtestTransactionClient = new BacktestTransactionClient();
        AlgoExecutor algoExecutor = new AlgoExecutor();

        algoExecutor.executeAlgo(ts, csvDataClient, backtestTransactionClient);
    }
}
