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
package com.ibm.websphere.samples.daytrader.jaxrs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.websphere.samples.daytrader.messaging.MessageProducerService;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.TimerStat;

/**
 * REST Resource for testing and monitoring messaging functionality.
 *
 * MIGRATION NOTES:
 *
 * This replaces PingServlet2MDBQueue and PingServlet2MDBTopic servlets
 * which were used to test JMS messaging in the original application.
 *
 * Original endpoints:
 *   /ejb3/PingServlet2MDBQueue - Send test message to TradeBrokerQueue
 *   /ejb3/PingServlet2MDBTopic - Send test message to TradeStreamerTopic
 *
 * Spring Boot endpoints:
 *   /rest/messaging/ping/broker - Send ping to broker queue
 *   /rest/messaging/ping/streamer - Send ping to streamer topic
 *   /rest/messaging/stats - Get messaging statistics
 */
@RestController
@RequestMapping("/messaging")
public class MessagingResource {

    @Autowired
    MessageProducerService messageProducer;

    // Use singleton pattern - original MDBStats is not CDI-managed
    private final MDBStats mdbStats = MDBStats.getInstance();

    /**
     * Send a ping message to the trade broker queue.
     * Replaces PingServlet2MDBQueue functionality.
     */
    @PostMapping("/ping/broker")
    public ResponseEntity<?> pingBroker(@RequestParam(value = "message", required = false) String message) {
        if (message == null || message.isEmpty()) {
            message = "Ping from MessagingResource at " + System.currentTimeMillis();
        }

        messageProducer.sendBrokerPing(message);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "sent");
        response.put("destination", "trade-broker-queue");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Send a ping message to the trade streamer topic.
     * Replaces PingServlet2MDBTopic functionality.
     */
    @PostMapping("/ping/streamer")
    public ResponseEntity<?> pingStreamer(@RequestParam(value = "message", required = false) String message) {
        if (message == null || message.isEmpty()) {
            message = "Ping from MessagingResource at " + System.currentTimeMillis();
        }

        messageProducer.sendStreamerPing(message);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "sent");
        response.put("destination", "trade-streamer-topic");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Get messaging statistics.
     * Shows statistics for processed messages, timing information, etc.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> response = new HashMap<>();

        // MDBStats extends HashMap<String, TimerStat> so iterate directly
        Map<String, Map<String, Object>> formattedStats = new HashMap<>();

        for (Map.Entry<String, TimerStat> entry : mdbStats.entrySet()) {
            TimerStat stat = entry.getValue();
            Map<String, Object> statMap = new HashMap<>();
            statMap.put("count", stat.getCount());
            statMap.put("minSeconds", stat.getMinSecs());
            statMap.put("maxSeconds", stat.getMaxSecs());
            statMap.put("avgSeconds", stat.getAvgSecs());
            formattedStats.put(entry.getKey(), statMap);
        }

        response.put("statistics", formattedStats);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Reset messaging statistics.
     */
    @PostMapping("/stats/reset")
    public ResponseEntity<?> resetStats() {
        mdbStats.reset();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "reset");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
