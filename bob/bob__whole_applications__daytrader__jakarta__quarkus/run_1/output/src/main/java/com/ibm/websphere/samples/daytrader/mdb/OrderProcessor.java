/**
 * (C) Copyright IBM Corporation 2015.
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
package com.ibm.websphere.samples.daytrader.mdb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.util.TradeRunTimeModeLiteral;

import java.io.StringReader;

/**
 * OrderProcessor processes async order completion requests from the trade-broker-queue.
 * Migrated from DTBroker3MDB (Jakarta EE MDB) to Quarkus Reactive Messaging.
 */
@ApplicationScoped
@Trace
@Transactional
public class OrderProcessor {
    private final MDBStats mdbStats;
    private int statInterval = 10000;

    @Inject
    @Any
    Instance<TradeServices> services;

    private TradeServices trade;

    public OrderProcessor() {
        if (statInterval <= 0) {
            statInterval = 10000;
        }
        mdbStats = MDBStats.getInstance();
    }

    @jakarta.annotation.PostConstruct
    void bootstrapTradeServices() {
        trade = services.select(new TradeRunTimeModeLiteral(TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()])).get();
    }

    /**
     * Process incoming order messages from the trade-broker-queue channel.
     * 
     * @param payload JSON message containing order details
     */
    @Incoming("trade-broker-queue")
    public void onMessage(String payload) {
        try {
            Log.trace("OrderProcessor:onMessage -- received message -->" + payload);

            // Parse JSON message
            JsonReader jsonReader = Json.createReader(new StringReader(payload));
            JsonObject message = jsonReader.readObject();
            jsonReader.close();

            String command = message.getString("command", null);
            if (command == null) {
                Log.debug("OrderProcessor:onMessage -- received message with null command. Message-->" + payload);
                return;
            }

            if (command.equalsIgnoreCase("neworder")) {
                /* Get the Order ID and complete the Order */
                int orderID = message.getInt("orderID");
                boolean twoPhase = message.getBoolean("twoPhase");
                boolean direct = message.getBoolean("direct", false);
                long publishTime = message.getJsonNumber("publishTime").longValue();
                long receiveTime = System.currentTimeMillis();

                try {
                    Log.trace("OrderProcessor:onMessage - completing order " + orderID + " twoPhase=" + twoPhase + " direct=" + direct);

                    trade.completeOrder(orderID, twoPhase);

                    TimerStat currentStats = mdbStats.addTiming("OrderProcessor:neworder", publishTime, receiveTime);

                    if ((currentStats.getCount() % statInterval) == 0) {
                        Log.log(" OrderProcessor: processed " + statInterval + " stock trading orders." +
                                " Total NewOrders process = " + currentStats.getCount() +
                                " Time (in seconds):" +
                                " min: " + currentStats.getMinSecs() +
                                " max: " + currentStats.getMaxSecs() +
                                " avg: " + currentStats.getAvgSecs());
                    }
                } catch (Exception e) {
                    Log.error("OrderProcessor:onMessage Exception completing order: " + orderID + "\n", e);
                    throw new RuntimeException("Failed to complete order: " + orderID, e);
                }
            } else if (command.equalsIgnoreCase("ping")) {
                Log.trace("OrderProcessor:onMessage received test command -- message: " + payload);

                long publishTime = message.getJsonNumber("publishTime").longValue();
                long receiveTime = System.currentTimeMillis();

                TimerStat currentStats = mdbStats.addTiming("OrderProcessor:ping", publishTime, receiveTime);

                if ((currentStats.getCount() % statInterval) == 0) {
                    Log.log(" OrderProcessor: received " + statInterval + " ping messages." +
                            " Total ping message count = " + currentStats.getCount() +
                            " Time (in seconds):" +
                            " min: " + currentStats.getMinSecs() +
                            " max: " + currentStats.getMaxSecs() +
                            " avg: " + currentStats.getAvgSecs());
                }
            } else {
                Log.error("OrderProcessor:onMessage - unknown message request command-->" + command + "<-- message=" + payload);
            }
        } catch (Throwable t) {
            // Handle all exceptions
            Log.error("OrderProcessor: Error processing message", t);
            throw new RuntimeException("Failed to process order message", t);
        }
    }
}

// Made with Bob
