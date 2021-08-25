package com.goquant.quantplatform.gateway.data;

import com.goquant.quantplatform.entity.BarData;
import org.apache.flink.api.java.ExecutionEnvironment;

import java.time.Instant;
import java.util.Set;

public abstract class BaseDataClient {
    // get recent ts data
    public abstract BarData getBarData(Instant ts, Set<String> symbols);

    public abstract void loadHistoricalDataSnapshot(Instant tsStart, Instant tsEnd);
}