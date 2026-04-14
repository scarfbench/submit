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
package com.ibm.websphere.samples.daytrader.jaxrs;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ibm.websphere.samples.daytrader.interfaces.QuotePriceChange;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;

@RestController
@RequestMapping("/rest/broadcastevents")
public class BroadcastResource {

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  @Autowired
  private RecentQuotePriceChangeList recentQuotePriceChangeList;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter register() {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

    // Send initial data
    try {
      if (recentQuotePriceChangeList.isEmpty()) {
        emitter.send(SseEmitter.event()
            .data("welcome!")
            .build());
      } else {
        emitter.send(SseEmitter.event()
            .data(recentQuotePriceChangeList.recentList(), MediaType.APPLICATION_JSON)
            .build());
      }
    } catch (IOException e) {
      emitter.completeWithError(e);
      return emitter;
    }

    // Register emitter
    emitters.add(emitter);

    // Remove emitter on completion or timeout
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError((e) -> emitters.remove(emitter));

    return emitter;
  }

  @Async
  @EventListener
  public void handleQuotePriceChangeEvent(@QuotePriceChange String event) {
    List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

    emitters.forEach(emitter -> {
      try {
        emitter.send(SseEmitter.event()
            .data(recentQuotePriceChangeList.recentList(), MediaType.APPLICATION_JSON)
            .build());
      } catch (Exception e) {
        deadEmitters.add(emitter);
      }
    });

    // Remove dead emitters
    emitters.removeAll(deadEmitters);
  }
}
