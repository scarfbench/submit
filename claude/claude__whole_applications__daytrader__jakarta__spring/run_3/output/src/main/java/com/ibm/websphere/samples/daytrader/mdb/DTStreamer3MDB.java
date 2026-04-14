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

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

/**
 * Converted from Message-Driven Bean to Spring Component.
 * Originally processed JMS messages from TradeStreamerTopic for quote updates.
 * Now provides a direct method to process quote streaming events.
 */
@Component
@Transactional
@Trace
public class DTStreamer3MDB {

  private final MDBStats mdbStats;
  private int statInterval = 10000;

  public DTStreamer3MDB() {
    Log.trace("DTStreamer3MDB:DTStreamer3MDB()");

    if (statInterval <= 0) {
      statInterval = 10000;
    }
    mdbStats = MDBStats.getInstance();
  }

  /**
   * Process a quote update message. This was originally called by JMS onMessage.
   * Now can be called directly from application code.
   *
   * @param command The command type ("updateQuote" or "ping")
   * @param symbol The stock symbol being updated
   * @param price The current price
   * @param oldPrice The old price
   * @param publishTime The time the message was published
   */
  public void processQuoteUpdate(String command, String symbol, String price, String oldPrice, long publishTime) {
    try {
      Log.trace("DTStreamer3MDB:processQuoteUpdate -- command=" + command);

      if (command == null) {
        Log.debug("DTStreamer3MDB:processQuoteUpdate -- received null command");
        return;
      }

      if (command.equalsIgnoreCase("updateQuote")) {
        Log.trace("DTStreamer3MDB:processQuoteUpdate -- symbol=" + symbol +
                  " current price=" + price + " old price=" + oldPrice);

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
        Log.trace("DTStreamer3MDB:processQuoteUpdate received ping command");

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
        Log.error("DTStreamer3MDB:processQuoteUpdate - unknown command: " + command);
      }
    } catch (Throwable t) {
      Log.error("DTStreamer3MDB: Exception processing quote update", t);
      throw new RuntimeException("Error processing quote update", t);
    }
  }
}
