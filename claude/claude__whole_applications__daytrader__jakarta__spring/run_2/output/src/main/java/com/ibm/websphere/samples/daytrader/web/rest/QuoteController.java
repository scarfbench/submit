package com.ibm.websphere.samples.daytrader.web.rest;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for quote operations
 */
@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    @Autowired
    private TradeService tradeService;

    @GetMapping
    public ResponseEntity<?> getAllQuotes() {
        try {
            Collection<QuoteDataBean> quotes = tradeService.getAllQuotes();
            return ResponseEntity.ok(quotes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<?> getQuote(@PathVariable String symbol) {
        try {
            QuoteDataBean quote = tradeService.getQuote(symbol);
            if (quote == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Quote not found for symbol: " + symbol);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(quote);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> createQuote(@RequestBody Map<String, Object> request) {
        try {
            String symbol = (String) request.get("symbol");
            String companyName = (String) request.get("companyName");
            BigDecimal price;

            Object priceObj = request.get("price");
            if (priceObj instanceof Number) {
                price = new BigDecimal(priceObj.toString());
            } else if (priceObj instanceof String) {
                price = new BigDecimal((String) priceObj);
            } else {
                throw new IllegalArgumentException("Invalid price format");
            }

            if (symbol == null || companyName == null || price == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required fields: symbol, companyName, price");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            QuoteDataBean quote = tradeService.createQuote(symbol, companyName, price);
            return ResponseEntity.status(HttpStatus.CREATED).body(quote);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
