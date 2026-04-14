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

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "TradeStreamerTopic"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "NonDurable")
})
public class DTStreamer3MDB implements MessageListener {

    private final MDBStats mdbStats;
    private int statInterval = 10000;

    public DTStreamer3MDB() {
        Log.trace("DTStreamer3MDB:DTStreamer3MDB()");
        if (statInterval <= 0) {
            statInterval = 10000;
        }
        mdbStats = MDBStats.getInstance();
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String command = textMessage.getStringProperty("command");

            Log.trace("DTStreamer3MDB:onMessage -- received message -->" + textMessage.getText() + "command-->"
                + command + "<--");

            if (command == null) {
                Log.debug("DTStreamer3MDB:onMessage -- received message with null command. Message-->" + message);
                return;
            }
            if (command.equalsIgnoreCase("updateQuote")) {
                String symbol = textMessage.getStringProperty("symbol");
                String price = textMessage.getStringProperty("price");
                String oldPrice = textMessage.getStringProperty("oldPrice");

                Log.trace("DTStreamer3MDB:onMessage -- received message -->" + textMessage.getText() + "\n\t symbol = "
                    + symbol + "\n\t current price =" + price + "\n\t old price =" + oldPrice);

                long publishTime = textMessage.getLongProperty("publishTime");
                long receiveTime = System.currentTimeMillis();
                TimerStat currentStats = mdbStats.addTiming("DTStreamer3MDB:updateQuote", publishTime, receiveTime);

                if ((currentStats.getCount() % statInterval) == 0) {
                    Log.log(" DTStreamer3MDB: " + statInterval + " prices updated:" +
                        " Total message count = " + currentStats.getCount() +
                        " Time (in seconds):" +
                        " min: " + currentStats.getMinSecs() +
                        " max: " + currentStats.getMaxSecs() +
                        " avg: " + currentStats.getAvgSecs());
                }
            } else if (command.equalsIgnoreCase("ping")) {
                Log.trace("DTStreamer3MDB:onMessage  received ping command -- message: " + textMessage.getText());

                long publishTime = textMessage.getLongProperty("publishTime");
                long receiveTime = System.currentTimeMillis();
                TimerStat currentStats = mdbStats.addTiming("DTStreamer3MDB:ping", publishTime, receiveTime);

                if ((currentStats.getCount() % statInterval) == 0) {
                    Log.log(" DTStreamer3MDB: received " + statInterval + " ping messages." +
                        " Total message count = " + currentStats.getCount() +
                        " Time (in seconds):" +
                        " min: " + currentStats.getMinSecs() +
                        " max: " + currentStats.getMaxSecs() +
                        " avg: " + currentStats.getAvgSecs());
                }
            } else {
                Log.error("DTStreamer3MDB:onMessage - unknown message request command-->" + command + "<-- message=" + textMessage.getText());
            }
        } catch (Throwable t) {
            Log.error("DTStreamer3MDB: Exception", t);
            throw new RuntimeException("DTStreamer3MDB processing failed", t);
        }
    }
}
