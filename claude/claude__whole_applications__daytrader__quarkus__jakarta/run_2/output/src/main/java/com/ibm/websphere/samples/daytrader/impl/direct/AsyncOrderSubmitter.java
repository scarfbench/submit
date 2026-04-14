/**
 * (C) Copyright IBM Corporation 2019.
 */
package com.ibm.websphere.samples.daytrader.impl.direct;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class AsyncOrderSubmitter {

    @Resource
    ManagedExecutorService executorService;

    @Inject
    private AsyncOrder asyncOrder;

    public Future<?> submitOrder(Integer orderID, boolean twoPhase) {
        asyncOrder.setProperties(orderID, twoPhase);
        return CompletableFuture.runAsync(asyncOrder, executorService);
    }
}
