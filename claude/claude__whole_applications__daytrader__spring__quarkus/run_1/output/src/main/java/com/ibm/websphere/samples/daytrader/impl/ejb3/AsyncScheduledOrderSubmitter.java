package com.ibm.websphere.samples.daytrader.impl.ejb3;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AsyncScheduledOrderSubmitter {

    @Inject
    @Named("ManagedScheduledTaskExecutor")
    private ScheduledExecutorService mes;

    @Inject
    private AsyncScheduledOrder asyncOrder;

    public Future<?> submitOrder(Integer orderID, boolean twoPhase) {
        asyncOrder.setProperties(orderID, twoPhase);
        return mes.schedule(asyncOrder, 500, TimeUnit.MILLISECONDS);
    }
}
