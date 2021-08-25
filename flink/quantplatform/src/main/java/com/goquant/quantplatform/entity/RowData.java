package com.goquant.quantplatform.entity;

import java.time.Instant;

public class RowData {
    public Instant ts;
    public String symbol;
    public double open;
    public double high;
    public double low;
    public double close;

    public RowData() {}

    public RowData setTs(Instant ts) {
        this.ts = ts;
        return this;
    }

    public RowData setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public RowData setOpen(double open) {
        this.open = open;
        return this;
    }

    public RowData setHigh(double high) {
        this.high = high;
        return this;
    }

    public RowData setLow(double low) {
        this.low = low;
        return this;
    }

    public RowData setClose(double close) {
        this.close = close;
        return this;
    }
}
