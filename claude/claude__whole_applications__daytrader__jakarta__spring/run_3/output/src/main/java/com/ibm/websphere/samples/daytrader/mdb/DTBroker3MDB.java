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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

/**
 * Converted from Message-Driven Bean to Spring Component.
 * Originally processed JMS messages from TradeBrokerQueue.
 * Now provides a direct method to process order completion requests.
 */
@Component
@Transactional
@Trace
public class DTBroker3MDB {
  private final MDBStats mdbStats;
  private int statInterval = 10000;

  @Autowired
  private TradeServices trade;

  public DTBroker3MDB() {
    if (statInterval <= 0) {
      statInterval = 10000;
    }
    mdbStats = MDBStats.getInstance();
  }

  /**
   * Process an order message. This was originally called by JMS onMessage.
   * Now can be called directly from application code.
   *
   * @param command The command type ("neworder" or "ping")
   * @param orderID The order ID to process (for neworder command)
   * @param twoPhase Whether to use two-phase processing
   * @param direct Whether this is a direct call
   * @param publishTime The time the message was published
   */
  public void processOrder(String command, Integer orderID, boolean twoPhase, boolean direct, long publishTime) {
    try {
      Log.trace("TradeBroker:processOrder -- command=" + command + " orderID=" + orderID);

      if (command == null) {
        Log.debug("DTBroker3MDB:processOrder -- received null command");
        return;
      }

      if (command.equalsIgnoreCase("neworder")) {
        /* Get the Order ID and complete the Order */
        long receiveTime = System.currentTimeMillis();

        try {
          Log.trace("DTBroker3MDB:processOrder - completing order " + orderID + " twoPhase=" + twoPhase + " direct=" + direct);

          trade.completeOrder(orderID, twoPhase);

          TimerStat currentStats = mdbStats.addTiming("DTBroker3MDB:neworder", publishTime, receiveTime);

          if ((currentStats.getCount() % statInterval) == 0) {
            Log.log(" DTBroker3MDB: processed " + statInterval + " stock trading orders." +
                " Total NewOrders process = " + currentStats.getCount() +
                " Time (in seconds):" +
                " min: " + currentStats.getMinSecs() +
                " max: " + currentStats.getMaxSecs() +
                " avg: " + currentStats.getAvgSecs());
          }
        } catch (Exception e) {
          Log.error("DTBroker3MDB:processOrder Exception completing order: " + orderID + "\n", e);
          throw new RuntimeException("Error completing order: " + orderID, e);
        }
      } else if (command.equalsIgnoreCase("ping")) {
        Log.trace("DTBroker3MDB:processOrder received test command (ping)");

        long receiveTime = System.currentTimeMillis();

        TimerStat currentStats = mdbStats.addTiming("DTBroker3MDB:ping", publishTime, receiveTime);

        if ((currentStats.getCount() % statInterval) == 0) {
          Log.log(" DTBroker3MDB: received " + statInterval + " ping messages." +
              " Total ping message count = " + currentStats.getCount() +
              " Time (in seconds):" +
              " min: " + currentStats.getMinSecs() +
              " max: " + currentStats.getMaxSecs() +
              " avg: " + currentStats.getAvgSecs());
        }
      } else {
        Log.error("DTBroker3MDB:processOrder - unknown message request command-->" + command);
      }
    } catch (Throwable t) {
      Log.error("DTBroker3MDB: Error processing order", t);
      throw new RuntimeException("Error processing order", t);
    }
  }
}
