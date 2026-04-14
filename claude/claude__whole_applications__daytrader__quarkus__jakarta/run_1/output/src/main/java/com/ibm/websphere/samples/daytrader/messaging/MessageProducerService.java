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

import java.math.BigDecimal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import com.ibm.websphere.samples.daytrader.util.Log;

/**
 * MIGRATION: Quarkus SmallRye Reactive Messaging -> Jakarta EE CDI Events
 *
 * In the Quarkus version, this used @Channel + Emitter from SmallRye Reactive Messaging.
 * In Jakarta EE, we use CDI Events to decouple the message producer from consumers.
 * The DTBroker3MDB and DTStreamer3MDB observe these events.
 */
@ApplicationScoped
public class MessageProducerService {

    @Inject
    Event<OrderMessage> brokerEvent;

    @Inject
    Event<QuoteUpdateMessage> streamerEvent;

    public void queueOrderForProcessing(Integer orderID, boolean twoPhase) {
        OrderMessage message = OrderMessage.newOrder(orderID, twoPhase);
        Log.trace("MessageProducerService:queueOrderForProcessing - sending order " + orderID);
        brokerEvent.fireAsync(message);
    }

    public void sendBrokerPing(String text) {
        OrderMessage message = OrderMessage.ping(text);
        Log.trace("MessageProducerService:sendBrokerPing - sending ping");
        brokerEvent.fireAsync(message);
    }

    public void publishQuoteUpdate(String symbol, BigDecimal newPrice, BigDecimal oldPrice) {
        QuoteUpdateMessage message = QuoteUpdateMessage.quoteUpdate(symbol, newPrice, oldPrice);
        Log.trace("MessageProducerService:publishQuoteUpdate - publishing update for " + symbol);
        streamerEvent.fireAsync(message);
    }

    public void publishQuotePriceChange(String symbol, String company, BigDecimal price,
            BigDecimal oldPrice, BigDecimal open, BigDecimal low, BigDecimal high,
            double volume, BigDecimal changeFactor, double sharesTraded) {
        QuoteUpdateMessage message = QuoteUpdateMessage.quoteUpdate(symbol, price, oldPrice);
        message.setCompany(company);
        message.setOpen(open);
        message.setLow(low);
        message.setHigh(high);
        message.setVolume(volume);
        message.setChangeFactor(changeFactor);
        message.setSharesTraded(sharesTraded);
        Log.trace("MessageProducerService:publishQuotePriceChange - publishing update for " + symbol);
        streamerEvent.fireAsync(message);
    }

    public void sendStreamerPing(String text) {
        QuoteUpdateMessage message = QuoteUpdateMessage.ping(text);
        Log.trace("MessageProducerService:sendStreamerPing - sending ping");
        streamerEvent.fireAsync(message);
    }
}
