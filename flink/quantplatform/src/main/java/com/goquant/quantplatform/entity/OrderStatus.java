package com.goquant.quantplatform.entity;

public enum OrderStatus {
    NOT_SUBMIT(0), QUEUE(1), SUBMITTED(2), CANCELED(3), EXECUTED(4);

    private final int value;

    private OrderStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
