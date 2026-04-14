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
import jakarta.enterprise.event.ObservesAsync;

import com.ibm.websphere.samples.daytrader.messaging.QuoteUpdateMessage;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

@ApplicationScoped
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

  public void onMessage(@ObservesAsync QuoteUpdateMessage message) {
    try {
      Log.trace("DTStreamer3MDB:onMessage -- received message -->" + message.getText() + "command-->"
          + message.getCommand() + "<--");

      String command = message.getCommand();
      if (command == null) {
        Log.debug("DTStreamer3MDB:onMessage -- received message with null command. Message-->" + message);
        return;
      }
      if (command.equalsIgnoreCase("updateQuote")) {
        Log.trace("DTStreamer3MDB:onMessage -- received message -->" + message.getText() + "\n\t symbol = "
            + message.getSymbol() + "\n\t current price =" + message.getPrice() + "\n\t old price ="
            + message.getOldPrice());

        long publishTime = message.getPublishTime();
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
        Log.trace("DTStreamer3MDB:onMessage  received ping command -- message: " + message.getText());

        long publishTime = message.getPublishTime();
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
        Log.error("DTStreamer3MDB:onMessage - unknown message request command-->" + command + "<-- message=" + message.getText());
      }
    } catch (Throwable t) {
      Log.error("DTStreamer3MDB: Exception", t);
      throw new RuntimeException("DTStreamer3MDB processing failed", t);
    }
  }
}
