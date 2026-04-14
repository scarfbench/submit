package com.ibm.websphere.samples.daytrader.web.rest;

import com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean;
import com.ibm.websphere.samples.daytrader.service.TradeDBService;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private TradeDBService tradeDBService;

    @GetMapping
    public ResponseEntity<?> getConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("runtimeMode", "Spring Boot");
            config.put("maxUsers", TradeConfig.getMAX_USERS());
            config.put("maxQuotes", TradeConfig.getMAX_QUOTES());
            config.put("orderProcessingMode", TradeConfig.getOrderProcessingMode());
            config.put("runtimeModeNames", TradeConfig.getRunTimeModeNames());
            config.put("orderProcessingModeNames", TradeConfig.getOrderProcessingModeNames());
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/buildDB")
    public ResponseEntity<?> buildDB() {
        try {
            String result = tradeDBService.buildDB();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Success");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "Error");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/resetTrade")
    public ResponseEntity<?> resetTrade() {
        try {
            RunStatsDataBean stats = tradeDBService.resetTrade(true);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
