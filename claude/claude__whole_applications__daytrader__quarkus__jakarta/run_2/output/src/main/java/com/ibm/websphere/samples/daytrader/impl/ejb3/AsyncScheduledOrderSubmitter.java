/**
 * (C) Copyright IBM Corporation 2019.
 */
package com.ibm.websphere.samples.daytrader.impl.ejb3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class AsyncScheduledOrderSubmitter {

    @Resource
    ManagedExecutorService executorService;

    @Inject
    private AsyncScheduledOrder asyncOrder;

    public Future<?> submitOrder(Integer orderID, boolean twoPhase) {
        asyncOrder.setProperties(orderID, twoPhase);
        return CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                asyncOrder.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executorService);
    }
}
