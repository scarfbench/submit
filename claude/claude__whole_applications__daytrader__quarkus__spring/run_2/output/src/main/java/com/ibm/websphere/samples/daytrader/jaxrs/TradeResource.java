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

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

/**
 * Main Trade REST API resource.
 * Provides REST endpoints for core trading operations.
 */
@RestController
@RequestMapping("/rest/trade")
public class TradeResource {

    @Autowired
    TradeServices tradeService;

    @GetMapping("/market")
    public ResponseEntity<?> getMarketSummary() {
        try {
            MarketSummaryDataBean summary = tradeService.getMarketSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("userID") String userID,
                         @RequestParam("password") String password) {
        try {
            AccountDataBean account = tradeService.login(userID, password);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/logout/{userID}")
    public ResponseEntity<?> logout(@PathVariable("userID") String userID) {
        try {
            tradeService.logout(userID);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/account/{userID}")
    public ResponseEntity<?> getAccount(@PathVariable("userID") String userID) {
        try {
            AccountDataBean account = tradeService.getAccountData(userID);
            if (account == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/account/{userID}/profile")
    public ResponseEntity<?> getAccountProfile(@PathVariable("userID") String userID) {
        try {
            AccountProfileDataBean profile = tradeService.getAccountProfileData(userID);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/account/{userID}/holdings")
    public ResponseEntity<?> getHoldings(@PathVariable("userID") String userID) {
        try {
            return ResponseEntity.ok(tradeService.getHoldings(userID));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/account/{userID}/orders")
    public ResponseEntity<?> getOrders(@PathVariable("userID") String userID) {
        try {
            return ResponseEntity.ok(tradeService.getOrders(userID));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestParam("userID") String userID,
                       @RequestParam("symbol") String symbol,
                       @RequestParam("quantity") double quantity) {
        try {
            OrderDataBean order = tradeService.buy(userID, symbol, quantity,
                    TradeConfig.getOrderProcessingMode());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sell(@RequestParam("userID") String userID,
                        @RequestParam("holdingID") Integer holdingID) {
        try {
            OrderDataBean order = tradeService.sell(userID, holdingID,
                    TradeConfig.getOrderProcessingMode());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam("userID") String userID,
                            @RequestParam("password") String password,
                            @RequestParam("fullname") String fullname,
                            @RequestParam("address") String address,
                            @RequestParam("email") String email,
                            @RequestParam("creditcard") String creditcard,
                            @RequestParam("openBalance") String openBalance) {
        try {
            BigDecimal balance = new BigDecimal(openBalance);
            AccountDataBean account = tradeService.register(userID, password, fullname,
                    address, email, creditcard, balance);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
