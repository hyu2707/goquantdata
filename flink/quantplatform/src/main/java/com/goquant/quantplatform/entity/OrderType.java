package com.goquant.quantplatform.entity;

public enum OrderType {
    LIMIT(0);

    private final int value;

    private OrderType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
