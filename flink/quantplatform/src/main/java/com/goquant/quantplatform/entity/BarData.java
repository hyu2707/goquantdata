package com.goquant.quantplatform.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class BarData {
    private static final Logger LOG = LoggerFactory.getLogger(BarData.class);

    public Instant ts;
    public Map<String, RowData> barDataMap = new HashMap<>();

    public BarData(){}

    public void addRow(RowData data) {
        barDataMap.put(data.symbol, data);
        if (ts != null && !ts.equals(data.ts)) {
            LOG.error("new row data ts not equal {} != {}", ts, data.ts);
        }
    }
}
