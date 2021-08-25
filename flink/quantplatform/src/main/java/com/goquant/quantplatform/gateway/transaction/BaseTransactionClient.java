package com.goquant.quantplatform.gateway.transaction;

import com.goquant.quantplatform.entity.OrderData;

import java.util.Set;

public abstract class BaseTransactionClient {

    public abstract void SubmitOrders(Set<OrderData> orderDataSet);



}
