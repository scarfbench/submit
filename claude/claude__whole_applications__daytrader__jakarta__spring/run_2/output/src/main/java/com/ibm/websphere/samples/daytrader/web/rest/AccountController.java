package com.ibm.websphere.samples.daytrader.web.rest;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
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
 * REST controller for account operations
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private TradeService tradeService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> request) {
        try {
            String userID = (String) request.get("userID");
            String password = (String) request.get("password");
            String fullname = (String) request.get("fullname");
            String address = (String) request.get("address");
            String email = (String) request.get("email");
            String creditcard = (String) request.get("creditcard");

            BigDecimal openBalance;
            Object balanceObj = request.get("openBalance");
            if (balanceObj instanceof Number) {
                openBalance = new BigDecimal(balanceObj.toString());
            } else if (balanceObj instanceof String) {
                openBalance = new BigDecimal((String) balanceObj);
            } else {
                throw new IllegalArgumentException("Invalid openBalance format");
            }

            if (userID == null || password == null || fullname == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required fields: userID, password, fullname");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            AccountDataBean account = tradeService.register(userID, password, fullname, address, email, creditcard, openBalance);
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> request) {
        try {
            String userID = (String) request.get("userID");
            String password = (String) request.get("password");

            if (userID == null || password == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required fields: userID, password");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            AccountDataBean account = tradeService.login(userID, password);
            if (account == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, Object> request) {
        try {
            String userID = (String) request.get("userID");

            if (userID == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required field: userID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            tradeService.logout(userID);
            Map<String, String> response = new HashMap<>();
            response.put("status", "Success");
            response.put("message", "User logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{userID}")
    public ResponseEntity<?> getAccountData(@PathVariable String userID) {
        try {
            AccountDataBean account = tradeService.getAccountData(userID);
            if (account == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account not found for userID: " + userID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{userID}/profile")
    public ResponseEntity<?> getAccountProfileData(@PathVariable String userID) {
        try {
            AccountProfileDataBean profile = tradeService.getAccountProfileData(userID);
            if (profile == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Profile not found for userID: " + userID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{userID}/profile")
    public ResponseEntity<?> updateAccountProfile(@PathVariable String userID, @RequestBody AccountProfileDataBean profileData) {
        try {
            // Ensure the userID matches
            if (profileData.getUserID() == null) {
                profileData.setUserID(userID);
            } else if (!userID.equals(profileData.getUserID())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "UserID mismatch between path and body");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            AccountProfileDataBean updatedProfile = tradeService.updateAccountProfile(profileData);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{userID}/holdings")
    public ResponseEntity<?> getHoldings(@PathVariable String userID) {
        try {
            Collection<HoldingDataBean> holdings = tradeService.getHoldings(userID);
            return ResponseEntity.ok(holdings);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{userID}/orders")
    public ResponseEntity<?> getOrders(@PathVariable String userID) {
        try {
            Collection<OrderDataBean> orders = tradeService.getOrders(userID);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
