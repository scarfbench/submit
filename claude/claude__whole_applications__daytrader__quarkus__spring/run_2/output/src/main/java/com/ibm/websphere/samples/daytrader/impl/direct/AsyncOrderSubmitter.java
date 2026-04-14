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

// MIGRATION: CDI -> Spring
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// MIGRATION: ManagedExecutorService -> Spring with simple ExecutorService
@Service
public class AsyncOrderSubmitter {

  private ExecutorService executorService = Executors.newCachedThreadPool();

  @Autowired
  private AsyncOrder asyncOrder;


  public Future<?> submitOrder(Integer orderID, boolean twoPhase) {
    asyncOrder.setProperties(orderID,twoPhase);
    return CompletableFuture.runAsync(asyncOrder, executorService);
  }
}
