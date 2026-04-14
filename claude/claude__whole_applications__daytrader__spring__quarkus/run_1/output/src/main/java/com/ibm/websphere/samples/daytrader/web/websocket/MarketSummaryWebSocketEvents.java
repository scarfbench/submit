package com.ibm.websphere.samples.daytrader.web.websocket;


import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

import com.ibm.websphere.samples.daytrader.events.MarketSummaryUpdateEvent;
import com.ibm.websphere.samples.daytrader.events.QuotePriceChangeEvent;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

import jakarta.websocket.Session;

@ApplicationScoped
public class MarketSummaryWebSocketEvents {

    @Inject
    private RecentQuotePriceChangeList recentQuotePriceChangeList;

    @Inject
    @jakarta.enterprise.inject.Any
    jakarta.enterprise.inject.Instance<TradeServices> tradeServicesInstance;

    private TradeServices tradeAction;

    @jakarta.annotation.PostConstruct
    void resolveTradeServices() {
        String key = TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()];
        for (TradeServices ts : tradeServicesInstance) {
            String beanName = getBeanName(ts);
            if (key.equals(beanName)) {
                this.tradeAction = ts;
                break;
            }
        }
        if (this.tradeAction == null) {
            java.util.List<String> available = new java.util.ArrayList<>();
            for (TradeServices ts : tradeServicesInstance) {
                available.add(getBeanName(ts));
            }
            throw new IllegalStateException("No TradeServices bean named '" + key + "' (available: " + available + ")");
        }
    }

    private String getBeanName(TradeServices ts) {
        // CDI proxy classes need getSuperclass() to find the actual class with annotations
        Class<?> clazz = ts.getClass();
        jakarta.inject.Named named = clazz.getAnnotation(jakarta.inject.Named.class);
        if (named == null && clazz.getSuperclass() != null) {
            named = clazz.getSuperclass().getAnnotation(jakarta.inject.Named.class);
        }
        if (named != null && !named.value().isEmpty()) {
            return named.value();
        }
        // Check superclass for qualifier annotations too
        Class<?> realClass = clazz;
        if (realClass.getSuperclass() != null && realClass.getSuperclass() != Object.class) {
            realClass = realClass.getSuperclass();
        }
        if (realClass.getAnnotation(com.ibm.websphere.samples.daytrader.interfaces.TradeJDBC.class) != null) {
            return "Direct (JDBC)";
        }
        if (realClass.getAnnotation(com.ibm.websphere.samples.daytrader.interfaces.TradeEJB.class) != null) {
            return "Full EJB3";
        }
        if (realClass.getAnnotation(com.ibm.websphere.samples.daytrader.interfaces.TradeSession2Direct.class) != null) {
            return "Session to Direct";
        }
        // Default to simple class name
        String simpleName = realClass.getSimpleName();
        return simpleName;
    }

    public void onStockChange(@ObservesAsync QuotePriceChangeEvent quotePriceChangeEvent) {
        Log.trace("MarketSummaryWebSocketEvents:onStockChange");
        quotePriceChangeEvent.payload();
        for (Session s : MarketSummaryWebSocket.getSessions()) {
            if (s.isOpen()) {
                s.getAsyncRemote().sendObject(recentQuotePriceChangeList.recentList());
            }
        }
    }

    public void onMarketSummaryUpdate(@ObservesAsync MarketSummaryUpdateEvent marketSummaryUpdateEvent) {
        Log.trace("MarketSummaryWebSocketEvents:onMarketSummaryUpdate");
        marketSummaryUpdateEvent.payload();
        try {
            jakarta.json.JsonObject mkSummary = tradeAction.getMarketSummary().toJSON();
            for (Session s : MarketSummaryWebSocket.getSessions()) {
                if (s.isOpen()) {
                    s.getAsyncRemote().sendText(mkSummary.toString());
                }
            }
        } catch (Exception e) {
            Log.error("MarketSummaryWebSocketEvents:onMarketSummaryUpdate error", e);
        }
    }
}
