package com.ibm.websphere.samples.daytrader.web.rest;

import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.service.TradeService;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for trade operations
 */
@RestController
@RequestMapping("/api/trade")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody Map<String, Object> request) {
        try {
            String userID = (String) request.get("userID");
            String symbol = (String) request.get("symbol");

            double quantity;
            Object quantityObj = request.get("quantity");
            if (quantityObj instanceof Number) {
                quantity = ((Number) quantityObj).doubleValue();
            } else if (quantityObj instanceof String) {
                quantity = Double.parseDouble((String) quantityObj);
            } else {
                throw new IllegalArgumentException("Invalid quantity format");
            }

            if (userID == null || symbol == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required fields: userID, symbol, quantity");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            OrderDataBean order = tradeService.buy(userID, symbol, quantity, TradeConfig.SYNCH);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sell(@RequestBody Map<String, Object> request) {
        try {
            String userID = (String) request.get("userID");

            Integer holdingID;
            Object holdingObj = request.get("holdingID");
            if (holdingObj instanceof Number) {
                holdingID = ((Number) holdingObj).intValue();
            } else if (holdingObj instanceof String) {
                holdingID = Integer.parseInt((String) holdingObj);
            } else {
                throw new IllegalArgumentException("Invalid holdingID format");
            }

            if (userID == null || holdingID == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required fields: userID, holdingID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            OrderDataBean order = tradeService.sell(userID, holdingID, TradeConfig.SYNCH);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/orders/{userID}/closed")
    public ResponseEntity<?> getClosedOrders(@PathVariable String userID) {
        try {
            Collection<OrderDataBean> orders = tradeService.getClosedOrders(userID);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
