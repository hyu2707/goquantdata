package com.goquant.quantplatform.controller;

import com.goquant.quantplatform.gateway.transaction.BacktestTransactionClient;
import com.goquant.quantplatform.gateway.data.CsvDataClient;

import java.time.Instant;

public class App {

    public static void main(String[] args) {
        Instant ts = Instant.now();

        CsvDataClient csvDataClient = new CsvDataClient();
        BacktestTransactionClient backtestTransactionClient = new BacktestTransactionClient();
        AlgoExecutor algoExecutor = new AlgoExecutor();

        algoExecutor.executeAlgo(ts, csvDataClient, backtestTransactionClient);
    }
}
