/**
 * (C) Copyright IBM Corporation 2019.
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
package com.ibm.websphere.samples.daytrader.impl.direct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.ibm.websphere.samples.daytrader.interfaces.TradeJDBC;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;

@Component
public class AsyncOrder implements Runnable {

  @Autowired
  @Qualifier("tradeDirect")
  @TradeJDBC
  TradeServices tradeService;

  @Autowired
  PlatformTransactionManager transactionManager;

  Integer orderID;
  boolean twoPhase;

  public void setProperties(Integer orderID, boolean twoPhase) {
    this.orderID = orderID;
    this.twoPhase =  twoPhase;
  }

  @Override
  public void run() {

    TransactionStatus txStatus = null;
    try {
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      txStatus = transactionManager.getTransaction(def);
      tradeService.completeOrder(orderID, twoPhase);
      transactionManager.commit(txStatus);
    } catch (Exception e) {

      if (txStatus != null) {
        try {
          transactionManager.rollback(txStatus);
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
      e.printStackTrace();
    }
  }
}
