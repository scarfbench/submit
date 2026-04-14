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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

// MIGRATION: Quarkus -> Spring Boot
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Scope("prototype")
public class AsyncScheduledOrderSubmitter {

  // MIGRATION: Quarkus @VirtualThreads ExecutorService -> Standard ExecutorService
  private final ExecutorService executorService = Executors.newCachedThreadPool();

  @Autowired
  private ApplicationContext applicationContext;


  public Future<?> submitOrder(Integer orderID, boolean twoPhase) {
    // MIGRATION: Get prototype-scoped bean from ApplicationContext
    AsyncScheduledOrder asyncOrder = applicationContext.getBean(AsyncScheduledOrder.class);
    asyncOrder.setProperties(orderID, twoPhase);
    // MIGRATION: Use CompletableFuture with delay instead of scheduled executor
    return CompletableFuture.runAsync(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(500);
        asyncOrder.run();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }, executorService);
  }
}
