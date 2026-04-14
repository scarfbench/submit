/**
 * (C) Copyright IBM Corporation 2015, 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.web.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;

import jakarta.json.JsonObject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * This class is a WebSocket EndPoint that sends the Market Summary in JSON form
 * and
 * encodes recent quote price changes when requested or when triggered by CDI
 * events.
 **/
@ApplicationScoped
@ServerEndpoint(value = "/marketsummary", encoders = { QuotePriceChangeListEncoder.class }, decoders = {
        ActionDecoder.class })
public class MarketSummaryWebSocket {

    @Inject
    RecentQuotePriceChangeList recentQuotePriceChangeList;

    private TradeServices tradeAction;

    @Inject
    @jakarta.enterprise.inject.Any
    jakarta.enterprise.inject.Instance<TradeServices> tradeServicesInstance;

    @jakarta.annotation.PostConstruct
    void resolveTradeServices() {
        String key = com.ibm.websphere.samples.daytrader.util.TradeConfig.getRunTimeModeNames()[com.ibm.websphere.samples.daytrader.util.TradeConfig.getRunTimeMode()];
        // Iterate through available TradeServices implementations to find the matching one
        for (TradeServices ts : tradeServicesInstance) {
            String beanName = getBeanName(ts);
            if (key.equals(beanName)) {
                this.tradeAction = ts;
                break;
            }
        }
        if (this.tradeAction == null) {
            List<String> available = new ArrayList<>();
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

    private static final List<Session> sessions = new CopyOnWriteArrayList<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    static List<Session> getSessions() {
        return sessions;
    }

    @OnOpen
    public void onOpen(final Session session, EndpointConfig ec) {
        Log.trace(
                "MarketSummaryWebSocket:onOpen -- session -->" + session + "<--");

        sessions.add(session);
        latch.countDown();
    }

    @OnMessage
    public void sendMarketSummary(
            ActionMessage message,
            Session currentSession) {
        String action = message.getDecodedAction();

        Log.trace(
                "MarketSummaryWebSocket:sendMarketSummary -- received -->" +
                        action +
                        "<--");

        // Make sure onopen is finished
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.error("MarketSummaryWebSocket:sendMarketSummary interrupted", e);
            return;
        } catch (Throwable t) {
            Log.error("MarketSummaryWebSocket:sendMarketSummary await failed", t);
            return;
        }

        if (action != null && action.equals("updateMarketSummary")) {
            try {
                JsonObject mkSummary = tradeAction.getMarketSummary().toJSON();

                Log.trace(
                        "MarketSummaryWebSocket:sendMarketSummary -- sending -->" +
                                mkSummary +
                                "<--");

                currentSession.getAsyncRemote().sendText(mkSummary.toString());
            } catch (Exception e) {
                Log.error("MarketSummaryWebSocket:sendMarketSummary error", e);
            }
        } else if (action != null && action.equals("updateRecentQuotePriceChange")) {
            if (!recentQuotePriceChangeList.isEmpty()) {
                currentSession
                        .getAsyncRemote()
                        .sendObject(recentQuotePriceChangeList.recentList());
            }
        }
    }

    @OnError
    public void onError(Throwable t, Session currentSession) {
        Log.trace(
                "MarketSummaryWebSocket:onError -- session -->" +
                        currentSession +
                        "<--");
        Log.error("MarketSummaryWebSocket:onError", t);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Log.trace(
                "MarketSummaryWebSocket:onClose -- session -->" + session + "<--");
        sessions.remove(session);
    }

    // Event handling moved to MarketSummaryWebSocketEvents to avoid proxying this endpoint
}
