package com.goquant.quantplatform.entity;

import java.time.Instant;

public class RowData {
    public Instant ts;
    public String symbol;
    public double open;
    public double high;
    public double low;
    public double close;
    public double vol;

    public RowData() {}

    public RowData(Instant ts, String symbol, double open, double high, double low, double close, double vol) {
        this.ts = ts;
        this.symbol = symbol;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.vol = vol;
    }

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

    @Override
    public String toString(){
        return String.format("t:%s,s:%s,o:%f,h:%f,l:%f,c:%f,v:%f",ts.toString(), symbol, open, high, low, close, vol);
    }
}
