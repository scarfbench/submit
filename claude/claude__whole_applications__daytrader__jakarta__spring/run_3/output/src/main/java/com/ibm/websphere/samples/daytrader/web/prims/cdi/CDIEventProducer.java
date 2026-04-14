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
package com.ibm.websphere.samples.daytrader.web.prims.cdi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class CDIEventProducer {

  // Use standard Java ExecutorService instead of ManagedExecutorService
  private ExecutorService executorService;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @PostConstruct
  public void init() {
    executorService = Executors.newCachedThreadPool();
  }

  @PreDestroy
  public void destroy() {
    if (executorService != null) {
      executorService.shutdown();
    }
  }

  public void produceSyncEvent() {
    eventPublisher.publishEvent(new HitEvent("hitCount++"));
  }

  public void produceAsyncEvent() {
    executorService.submit(() -> {
      eventPublisher.publishEvent(new HitAsyncEvent("hitCount++"));
    });
  }

  // Event classes for Spring
  public static class HitEvent {
    private final String message;
    public HitEvent(String message) {
      this.message = message;
    }
    public String getMessage() {
      return message;
    }
  }

  public static class HitAsyncEvent {
    private final String message;
    public HitAsyncEvent(String message) {
      this.message = message;
    }
    public String getMessage() {
      return message;
    }
  }

}
