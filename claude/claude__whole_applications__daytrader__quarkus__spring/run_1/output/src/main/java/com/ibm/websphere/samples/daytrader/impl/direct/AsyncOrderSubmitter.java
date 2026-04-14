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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

// MIGRATION: ManagedExecutorService -> Spring ApplicationContext for bean retrieval
@Component
public class AsyncOrderSubmitter {

  @Autowired
  private ApplicationContext applicationContext;


  public Future<?> submitOrder(Integer orderID, boolean twoPhase) {
    AsyncOrder asyncOrder = applicationContext.getBean(AsyncOrder.class);
    asyncOrder.setProperties(orderID, twoPhase);
    return CompletableFuture.runAsync(asyncOrder);
  }
}
