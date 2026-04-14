package com.ibm.websphere.samples.daytrader.jms;

import jakarta.enterprise.context.ApplicationScoped;
import com.ibm.websphere.samples.daytrader.util.Log;

@ApplicationScoped
public class DTStreamer3Listener {
    // JMS listener disabled in Quarkus migration - no embedded Artemis available
    // In Spring, this listened on "TradeStreamerTopic"
    public void onStreamerTopicEvent(Object message) {
        Log.trace("DTStreamer3Listener: JMS disabled in Quarkus migration");
    }
}