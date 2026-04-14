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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;

// MIGRATION: Quarkus @Dependent -> Spring @Component @Scope("prototype")
@Component
@Scope("prototype")
public class AsyncOrder implements Runnable {

  // MIGRATION: @Inject @TradeJDBC -> @Autowired @Qualifier("tradeDirect") @Lazy (breaks circular dep)
  @Autowired
  @Qualifier("tradeDirect")
  @Lazy
  TradeServices tradeService;
      
  Integer orderID;
  boolean twoPhase;
  
  public void setProperties(Integer orderID, boolean twoPhase) {
    this.orderID = orderID;
    this.twoPhase =  twoPhase;
  }     
  
  @Override
  @Transactional
  public void run() {
        
        
    try {  
      tradeService.completeOrder(orderID, twoPhase);      
    } catch (Exception e) {
      
      try {
        throw new Exception(e);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    } 
  }
}
