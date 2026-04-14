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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.ibm.websphere.samples.daytrader.util.Log;

/**
 * MIGRATION NOTES:
 *
 * Quarkus (SmallRye Reactive Messaging - Emitter Pattern):
 * --------------------------------------------------------
 * @Inject
 * @Channel("trade-broker-queue")
 * Emitter<OrderMessage> brokerEmitter;
 *
 * // Usage:
 * OrderMessage message = OrderMessage.newOrder(orderID, twoPhase);
 * brokerEmitter.send(message);
 *
 * Spring (ApplicationEventPublisher - In-Process Events):
 * -------------------------------------------------------
 * @Autowired
 * private ApplicationEventPublisher eventPublisher;
 *
 * // Usage:
 * OrderMessage message = OrderMessage.newOrder(orderID, twoPhase);
 * eventPublisher.publishEvent(new BrokerMessageEvent(this, message));
 *
 * Key Differences:
 * 1. No @Channel annotation - just ApplicationEventPublisher
 * 2. Messages wrapped in Spring ApplicationEvent subclasses
 * 3. Synchronous in-process event delivery by default
 * 4. No external messaging infrastructure required
 */
@Service
public class MessageProducerService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Queue a new order for asynchronous processing.
     * This replaces the JMS Queue send in the original DTBroker pattern.
     *
     * @param orderID the order ID to process
     * @param twoPhase whether to use two-phase commit
     */
    public void queueOrderForProcessing(Integer orderID, boolean twoPhase) {
        OrderMessage message = OrderMessage.newOrder(orderID, twoPhase);
        Log.trace("MessageProducerService:queueOrderForProcessing - sending order " + orderID);
        eventPublisher.publishEvent(new BrokerMessageEvent(this, message));
    }

    /**
     * Send a ping message to the trade broker queue.
     * Used for testing and performance measurement.
     *
     * @param text the ping message text
     */
    public void sendBrokerPing(String text) {
        OrderMessage message = OrderMessage.ping(text);
        Log.trace("MessageProducerService:sendBrokerPing - sending ping");
        eventPublisher.publishEvent(new BrokerMessageEvent(this, message));
    }

    /**
     * Publish a quote update to the streamer topic.
     * This replaces the JMS Topic publish in the original DTStreamer pattern.
     *
     * @param symbol the stock symbol
     * @param newPrice the new price
     * @param oldPrice the old price
     */
    public void publishQuoteUpdate(String symbol, BigDecimal newPrice, BigDecimal oldPrice) {
        QuoteUpdateMessage message = QuoteUpdateMessage.quoteUpdate(symbol, newPrice, oldPrice);
        Log.trace("MessageProducerService:publishQuoteUpdate - publishing update for " + symbol);
        eventPublisher.publishEvent(new StreamerMessageEvent(this, message));
    }

    /**
     * Publish a complete quote price change to the streamer topic.
     * This replaces the full JMS Topic publish in the original TradeDirect pattern.
     */
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
        eventPublisher.publishEvent(new StreamerMessageEvent(this, message));
    }

    /**
     * Send a ping message to the streamer topic.
     * Used for testing and performance measurement.
     *
     * @param text the ping message text
     */
    public void sendStreamerPing(String text) {
        QuoteUpdateMessage message = QuoteUpdateMessage.ping(text);
        Log.trace("MessageProducerService:sendStreamerPing - sending ping");
        eventPublisher.publishEvent(new StreamerMessageEvent(this, message));
    }
}
