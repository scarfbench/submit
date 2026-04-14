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
package com.ibm.websphere.samples.daytrader.service;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.repositories.AccountProfileRepository;
import com.ibm.websphere.samples.daytrader.repositories.AccountRepository;
import com.ibm.websphere.samples.daytrader.repositories.HoldingRepository;
import com.ibm.websphere.samples.daytrader.repositories.OrderRepository;
import com.ibm.websphere.samples.daytrader.repositories.QuoteRepository;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Service
public class TradeDBService {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountProfileRepository accountProfileRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public String buildDB() {
        try {
            // Create quotes
            for (int i = 0; i < TradeConfig.getMAX_QUOTES(); i++) {
                String symbol = "s:" + i;
                String companyName = "S" + i + " Incorporated";
                BigDecimal price = TradeConfig.rndBigDecimal(1000.0f);
                price = price.compareTo(new BigDecimal(0)) <= 0 ? new BigDecimal("1.00") : price;

                QuoteDataBean quoteData = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
                quoteRepository.save(quoteData);

                if (i % 1000 == 0) {
                    Log.log("TradeDBService:buildDB -- created " + i + " quotes");
                }
            }

            // Create users and accounts
            for (int i = 0; i < TradeConfig.getMAX_USERS(); i++) {
                String userID = "uid:" + i;
                String fullname = TradeConfig.rndFullName();
                String email = TradeConfig.rndEmail(userID);
                String address = TradeConfig.rndAddress();
                String creditcard = TradeConfig.rndCreditCard();
                BigDecimal openBalance = new BigDecimal("1000000.00");

                AccountProfileDataBean profile = new AccountProfileDataBean(userID, "xxx", fullname, address, email, creditcard);
                // Save profile first (it has no FK to account) - use returned managed entity
                profile = accountProfileRepository.save(profile);

                AccountDataBean account = new AccountDataBean(0, 0, null,
                        new Timestamp(System.currentTimeMillis()), openBalance, openBalance, userID);

                // Set the owning side FK reference (account -> profile)
                account.setProfile(profile);

                // Save account (owns the FK PROFILE_USERID)
                account = accountRepository.save(account);

                // Set inverse reference for in-memory consistency
                profile.setAccount(account);

                if (i % 1000 == 0) {
                    Log.log("TradeDBService:buildDB -- created " + i + " users");
                }
            }

            Log.log("TradeDBService:buildDB -- completed successfully. Created "
                    + TradeConfig.getMAX_QUOTES() + " quotes and "
                    + TradeConfig.getMAX_USERS() + " users.");

            return "Database built successfully with " + TradeConfig.getMAX_QUOTES()
                    + " quotes and " + TradeConfig.getMAX_USERS() + " users.";
        } catch (Exception e) {
            Log.error("TradeDBService:buildDB -- error building database", e);
            throw new RuntimeException("Error building database: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RunStatsDataBean resetTrade(boolean deleteAll) {
        RunStatsDataBean stats = new RunStatsDataBean();

        try {
            stats.setTradeUserCount((int) accountProfileRepository.count());
            stats.setTradeStockCount((int) quoteRepository.count());
            stats.setHoldingCount((int) holdingRepository.count());
            stats.setOrderCount((int) orderRepository.count());

            if (deleteAll) {
                orderRepository.deleteAll();
                holdingRepository.deleteAll();
                // Don't delete accounts and quotes - just reset them
            }

            Log.log("TradeDBService:resetTrade -- completed");
        } catch (Exception e) {
            Log.error("TradeDBService:resetTrade -- error", e);
            throw new RuntimeException("Error resetting trade: " + e.getMessage(), e);
        }

        return stats;
    }
}
