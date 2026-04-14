package com.ibm.websphere.samples.daytrader.jms;

import jakarta.enterprise.context.ApplicationScoped;
import com.ibm.websphere.samples.daytrader.util.Log;

@ApplicationScoped
public class DTBroker3Listener {
    // JMS listener disabled in Quarkus migration - no embedded Artemis available
    // In Spring, this listened on "TradeBrokerQueue"
    public void onBrokerQueueMessage(Object message) {
        Log.trace("DTBroker3Listener: JMS disabled in Quarkus migration");
    }
}