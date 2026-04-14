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

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import com.ibm.websphere.samples.daytrader.util.Log;

/**
 * JMS Message Producer Service for DayTrader.
 * Uses standard Jakarta JMS to send messages to queues and topics.
 */
@ApplicationScoped
public class MessageProducerService {

    @Resource(lookup = "jms/QueueConnectionFactory")
    private ConnectionFactory queueConnectionFactory;

    @Resource(lookup = "jms/TopicConnectionFactory")
    private ConnectionFactory topicConnectionFactory;

    @Resource(lookup = "jms/TradeBrokerQueue")
    private Queue tradeBrokerQueue;

    @Resource(lookup = "jms/TradeStreamerTopic")
    private Topic tradeStreamerTopic;

    public void queueOrderForProcessing(Integer orderID, boolean twoPhase) {
        Log.trace("MessageProducerService:queueOrderForProcessing - sending order " + orderID);
        try (JMSContext context = queueConnectionFactory.createContext()) {
            TextMessage message = context.createTextMessage("neworder");
            message.setStringProperty("command", "neworder");
            message.setIntProperty("orderID", orderID);
            message.setBooleanProperty("twoPhase", twoPhase);
            message.setLongProperty("publishTime", System.currentTimeMillis());
            context.createProducer().send(tradeBrokerQueue, message);
        } catch (Exception e) {
            Log.error("MessageProducerService:queueOrderForProcessing failed", e);
            throw new RuntimeException(e);
        }
    }

    public void sendBrokerPing(String text) {
        Log.trace("MessageProducerService:sendBrokerPing - sending ping");
        try (JMSContext context = queueConnectionFactory.createContext()) {
            TextMessage message = context.createTextMessage(text);
            message.setStringProperty("command", "ping");
            message.setLongProperty("publishTime", System.currentTimeMillis());
            context.createProducer().send(tradeBrokerQueue, message);
        } catch (Exception e) {
            Log.error("MessageProducerService:sendBrokerPing failed", e);
            throw new RuntimeException(e);
        }
    }

    public void publishQuoteUpdate(String symbol, BigDecimal newPrice, BigDecimal oldPrice) {
        Log.trace("MessageProducerService:publishQuoteUpdate - publishing update for " + symbol);
        try (JMSContext context = topicConnectionFactory.createContext()) {
            TextMessage message = context.createTextMessage("updateQuote");
            message.setStringProperty("command", "updateQuote");
            message.setStringProperty("symbol", symbol);
            message.setStringProperty("price", newPrice.toString());
            message.setStringProperty("oldPrice", oldPrice.toString());
            message.setLongProperty("publishTime", System.currentTimeMillis());
            context.createProducer().send(tradeStreamerTopic, message);
        } catch (Exception e) {
            Log.error("MessageProducerService:publishQuoteUpdate failed", e);
            throw new RuntimeException(e);
        }
    }

    public void publishQuotePriceChange(String symbol, String company, BigDecimal price,
            BigDecimal oldPrice, BigDecimal open, BigDecimal low, BigDecimal high,
            double volume, BigDecimal changeFactor, double sharesTraded) {
        Log.trace("MessageProducerService:publishQuotePriceChange - publishing update for " + symbol);
        try (JMSContext context = topicConnectionFactory.createContext()) {
            TextMessage message = context.createTextMessage("updateQuote");
            message.setStringProperty("command", "updateQuote");
            message.setStringProperty("symbol", symbol);
            message.setStringProperty("company", company);
            message.setStringProperty("price", price.toString());
            message.setStringProperty("oldPrice", oldPrice.toString());
            message.setStringProperty("open", open != null ? open.toString() : "");
            message.setStringProperty("low", low != null ? low.toString() : "");
            message.setStringProperty("high", high != null ? high.toString() : "");
            message.setDoubleProperty("volume", volume);
            message.setStringProperty("changeFactor", changeFactor != null ? changeFactor.toString() : "");
            message.setDoubleProperty("sharesTraded", sharesTraded);
            message.setLongProperty("publishTime", System.currentTimeMillis());
            context.createProducer().send(tradeStreamerTopic, message);
        } catch (Exception e) {
            Log.error("MessageProducerService:publishQuotePriceChange failed", e);
            throw new RuntimeException(e);
        }
    }

    public void sendStreamerPing(String text) {
        Log.trace("MessageProducerService:sendStreamerPing - sending ping");
        try (JMSContext context = topicConnectionFactory.createContext()) {
            TextMessage message = context.createTextMessage(text);
            message.setStringProperty("command", "ping");
            message.setLongProperty("publishTime", System.currentTimeMillis());
            context.createProducer().send(tradeStreamerTopic, message);
        } catch (Exception e) {
            Log.error("MessageProducerService:sendStreamerPing failed", e);
            throw new RuntimeException(e);
        }
    }
}
