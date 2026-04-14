/**
 * (C) Copyright IBM Corporation 2015, 2024.
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
package com.ibm.websphere.samples.daytrader.startup;

import java.math.BigDecimal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

/**
 * Populates the database with initial data on application startup.
 * This replaces the manual "Populate Database" action from the Jakarta EE version.
 */
@Component
public class DatabasePopulator {

    @PersistenceContext
    EntityManager em;

    @Value("${trade.db.populate.on.startup:true}")
    boolean populateOnStartup;

    @Value("${trade.max.users:50}")
    int maxUsers;

    @Value("${trade.max.quotes:100}")
    int maxQuotes;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        if (!populateOnStartup) {
            Log.log("Database population disabled via configuration");
            return;
        }

        // Check if database already has data (using native SQL since entity names are lowercase)
        Number quoteCount = (Number) em.createNativeQuery("SELECT COUNT(*) FROM quoteejb").getSingleResult();
        if (quoteCount.longValue() > 0) {
            Log.log("Database already populated with " + quoteCount + " quotes. Skipping population.");
            return;
        }

        Log.log("TradeBuildDB: Populating DayTrader Database on startup...");

        try {
            populateQuotes();
            populateUsers();
            Log.log("TradeBuildDB: Database population complete!");
        } catch (Exception e) {
            Log.error(e, "TradeBuildDB: Error populating database");
        }
    }

    private void populateQuotes() {
        Log.log("TradeBuildDB: Creating " + maxQuotes + " Quotes...");

        for (int i = 0; i < maxQuotes; i++) {
            String symbol = "s:" + i;
            String companyName = "S" + i + " Incorporated";
            BigDecimal price = new BigDecimal(TradeConfig.rndPrice());

            QuoteDataBean quote = new QuoteDataBean();
            quote.setSymbol(symbol);
            quote.setCompanyName(companyName);
            quote.setPrice(price);
            quote.setOpen(price);
            quote.setLow(price);
            quote.setHigh(price);
            quote.setChange(0.0);
            quote.setVolume(0.0);

            em.persist(quote);

            if (i % 10 == 0) {
                Log.log("Created quote: " + symbol);
            }
        }
        em.flush();
    }

    private void populateUsers() {
        Log.log("TradeBuildDB: Registering " + maxUsers + " Users...");

        for (int i = 0; i < maxUsers; i++) {
            String userID = "uid:" + i;
            String fullname = TradeConfig.rndFullName();
            String email = TradeConfig.rndEmail(userID);
            String address = TradeConfig.rndAddress();
            String creditcard = TradeConfig.rndCreditCard();
            BigDecimal initialBalance = new BigDecimal((double) (TradeConfig.rndInt(100000)) + 200000);

            if (i == 0) {
                initialBalance = new BigDecimal(1000000); // uid:0 starts with a cool million.
            }

            // Create profile
            AccountProfileDataBean profile = new AccountProfileDataBean();
            profile.setUserID(userID);
            profile.setPassword("xxx");
            profile.setFullName(fullname);
            profile.setAddress(address);
            profile.setEmail(email);
            profile.setCreditCard(creditcard);
            em.persist(profile);

            // Create account
            AccountDataBean account = new AccountDataBean();
            account.setProfile(profile);
            account.setCreationDate(new java.sql.Timestamp(System.currentTimeMillis()));
            account.setLastLogin(new java.sql.Timestamp(System.currentTimeMillis()));
            account.setBalance(initialBalance);
            account.setOpenBalance(initialBalance);
            account.setLoginCount(0);
            account.setLogoutCount(0);
            em.persist(account);

            if (i % 10 == 0) {
                Log.log("Registered user: " + userID + " with balance: " + initialBalance);
            }
        }
        em.flush();
    }
}
