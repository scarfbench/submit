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
package com.ibm.websphere.samples.daytrader.impl.ejb3;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;


@Component
public class AsyncScheduledOrder implements Runnable {

  TradeServices tradeService;

  Integer orderID;
  boolean twoPhase;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  public AsyncScheduledOrder(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void setProperties(Integer orderID, boolean twoPhase) {
    this.orderID = orderID;
    this.twoPhase =  twoPhase;
    // Get the appropriate TradeServices implementation based on runtime mode
    String beanName = TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()];
    tradeService = applicationContext.getBean(beanName, TradeServices.class);
  }

  @Override
  public void run() {


    try {
      tradeService.completeOrder(orderID, twoPhase);

    } catch (Exception e) {

      e.printStackTrace();
    }
  }
}
