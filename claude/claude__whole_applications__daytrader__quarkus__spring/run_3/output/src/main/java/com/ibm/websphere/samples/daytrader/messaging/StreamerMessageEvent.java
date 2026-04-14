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
package com.ibm.websphere.samples.daytrader.messaging;

import org.springframework.context.ApplicationEvent;

/**
 * Spring ApplicationEvent wrapper for QuoteUpdateMessage.
 *
 * This event is published to the Spring application context and consumed
 * by DTStreamer3MDB using @EventListener.
 *
 * Replaces the Quarkus @Channel("trade-streamer-topic") in-memory channel.
 */
public class StreamerMessageEvent extends ApplicationEvent {
    private final QuoteUpdateMessage message;

    public StreamerMessageEvent(Object source, QuoteUpdateMessage message) {
        super(source);
        this.message = message;
    }

    public QuoteUpdateMessage getMessage() {
        return message;
    }
}
