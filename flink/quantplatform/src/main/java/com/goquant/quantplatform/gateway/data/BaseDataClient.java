package com.goquant.quantplatform.gateway.data;

import com.goquant.quantplatform.common.GoquantConfig;
import com.goquant.quantplatform.entity.BarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public abstract class BaseDataClient {
    private static final Logger LOG = LoggerFactory.getLogger(BaseDataClient.class);

    GoquantConfig cfg;
    protected boolean isBacktest;
    protected Instant historicalDataStart;
    protected Instant historicalDataEnd;

    protected Map<Instant, BarData> barDataMap;

    public BaseDataClient(GoquantConfig cfg){
        isBacktest = cfg.getIsbacktest();
        historicalDataStart = cfg.getHistoricalDataStartDay();
        historicalDataEnd = cfg.getHistoricalDataEndDay();
        this.cfg = cfg;
    };

    public void start(){
        LOG.info("BaseDataClient start() called");
        if (isBacktest) {
            LOG.info("loading historical data start");
            barDataMap = loadHistoricalDataSnapshot(historicalDataStart, historicalDataEnd);
            LOG.info("loaded historical data size: {}", barDataMap.size());
        } else {
            startStreamData();
        }
        LOG.info("BaseDataClient start() end");
    };

    // get recent ts data
    public BarData getBarData(Instant ts) {
        LOG.debug("getBarData called, ts: {}", ts);
        LOG.debug("barDataMap: {}", barDataMap);
        return barDataMap.get(ts);
    };

    protected abstract Map<Instant, BarData> loadHistoricalDataSnapshot(Instant tsStart, Instant tsEnd);

    protected abstract void startStreamData();
}