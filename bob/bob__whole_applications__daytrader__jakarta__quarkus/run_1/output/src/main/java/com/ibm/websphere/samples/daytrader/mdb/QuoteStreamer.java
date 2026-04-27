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
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

import java.io.StringReader;

/**
 * QuoteStreamer streams quote price updates from the trade-streamer-topic.
 * Migrated from DTStreamer3MDB (Jakarta EE MDB) to Quarkus Reactive Messaging.
 */
@ApplicationScoped
@Trace
@Transactional
public class QuoteStreamer {

    private final MDBStats mdbStats;
    private int statInterval = 10000;

    /** Creates a new instance of QuoteStreamer */
    public QuoteStreamer() {
        Log.trace("QuoteStreamer:QuoteStreamer()");

        if (statInterval <= 0) {
            statInterval = 10000;
        }
        mdbStats = MDBStats.getInstance();
    }

    /**
     * Process incoming quote update messages from the trade-streamer-topic channel.
     * 
     * @param payload JSON message containing quote update details
     */
    @Incoming("trade-streamer-topic")
    public void onMessage(String payload) {
        try {
            Log.trace("QuoteStreamer:onMessage -- received message -->" + payload);

            // Parse JSON message
            JsonReader jsonReader = Json.createReader(new StringReader(payload));
            JsonObject message = jsonReader.readObject();
            jsonReader.close();

            String command = message.getString("command", null);
            if (command == null) {
                Log.debug("QuoteStreamer:onMessage -- received message with null command. Message-->" + payload);
                return;
            }

            if (command.equalsIgnoreCase("updateQuote")) {
                String symbol = message.getString("symbol", "");
                String price = message.getString("price", "");
                String oldPrice = message.getString("oldPrice", "");

                Log.trace("QuoteStreamer:onMessage -- received message -->" + payload + "\n\t symbol = "
                        + symbol + "\n\t current price =" + price + "\n\t old price =" + oldPrice);

                long publishTime = message.getJsonNumber("publishTime").longValue();
                long receiveTime = System.currentTimeMillis();

                TimerStat currentStats = mdbStats.addTiming("QuoteStreamer:updateQuote", publishTime, receiveTime);

                if ((currentStats.getCount() % statInterval) == 0) {
                    Log.log(" QuoteStreamer: " + statInterval + " prices updated:" +
                            " Total message count = " + currentStats.getCount() +
                            " Time (in seconds):" +
                            " min: " + currentStats.getMinSecs() +
                            " max: " + currentStats.getMaxSecs() +
                            " avg: " + currentStats.getAvgSecs());
                }
            } else if (command.equalsIgnoreCase("ping")) {
                Log.trace("QuoteStreamer:onMessage received ping command -- message: " + payload);

                long publishTime = message.getJsonNumber("publishTime").longValue();
                long receiveTime = System.currentTimeMillis();

                TimerStat currentStats = mdbStats.addTiming("QuoteStreamer:ping", publishTime, receiveTime);

                if ((currentStats.getCount() % statInterval) == 0) {
                    Log.log(" QuoteStreamer: received " + statInterval + " ping messages." +
                            " Total message count = " + currentStats.getCount() +
                            " Time (in seconds):" +
                            " min: " + currentStats.getMinSecs() +
                            " max: " + currentStats.getMaxSecs() +
                            " avg: " + currentStats.getAvgSecs());
                }
            } else {
                Log.error("QuoteStreamer:onMessage - unknown message request command-->" + command + "<-- message=" + payload);
            }
        } catch (Throwable t) {
            // Handle all exceptions
            Log.error("QuoteStreamer: Exception", t);
            throw new RuntimeException("Failed to process quote update message", t);
        }
    }
}

// Made with Bob
