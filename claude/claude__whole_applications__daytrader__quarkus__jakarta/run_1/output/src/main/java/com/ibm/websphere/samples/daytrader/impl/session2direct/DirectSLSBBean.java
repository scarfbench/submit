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
package com.ibm.websphere.samples.daytrader.impl.session2direct;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.Future;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.RuntimeMode;
import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.interfaces.TradeJDBC;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.interfaces.TradeSession2Direct;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@ApplicationScoped
@TradeSession2Direct
@RuntimeMode("Session to Direct")
@Trace
public class DirectSLSBBean implements TradeServices {

    @Inject
    @TradeJDBC
    Instance<TradeServices> tradeDirect;

    private TradeServices getTradeDirect() {
        TradeServices ts = tradeDirect.get();
        ts.setInSession(true);
        return ts;
    }

    @Override
    public MarketSummaryDataBean getMarketSummary() throws Exception {
        return getTradeDirect().getMarketSummary();
    }

    @Override
    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
        return getTradeDirect().buy(userID, symbol, quantity, orderProcessingMode);
    }

    @Override
    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
        return getTradeDirect().sell(userID, holdingID, orderProcessingMode);
    }

    @Override
    public void queueOrder(Integer orderID, boolean twoPhase) throws Exception {
        getTradeDirect().queueOrder(orderID, twoPhase);
    }

    @Override
    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception {
        return getTradeDirect().completeOrder(orderID, twoPhase);
    }

    @Override
    public Future<OrderDataBean> completeOrderAsync(Integer orderID, boolean twoPhase) throws Exception {
        return getTradeDirect().completeOrderAsync(orderID, twoPhase);
    }

    @Override
    public void cancelOrder(Integer orderID, boolean twoPhase) throws Exception {
        getTradeDirect().cancelOrder(orderID, twoPhase);
    }

    @Override
    public void orderCompleted(String userID, Integer orderID) throws Exception {
        getTradeDirect().orderCompleted(userID, orderID);
    }

    @Override
    public Collection<?> getOrders(String userID) throws Exception {
        return getTradeDirect().getOrders(userID);
    }

    @Override
    public Collection<?> getClosedOrders(String userID) throws Exception {
        return getTradeDirect().getClosedOrders(userID);
    }

    @Override
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
        return getTradeDirect().createQuote(symbol, companyName, price);
    }

    @Override
    public QuoteDataBean getQuote(String symbol) throws Exception {
        return getTradeDirect().getQuote(symbol);
    }

    @Override
    public Collection<?> getAllQuotes() throws Exception {
        return getTradeDirect().getAllQuotes();
    }

    @Override
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal newPrice, double sharesTraded) throws Exception {
        return getTradeDirect().updateQuotePriceVolume(symbol, newPrice, sharesTraded);
    }

    @Override
    public Collection<HoldingDataBean> getHoldings(String userID) throws Exception {
        return getTradeDirect().getHoldings(userID);
    }

    @Override
    public HoldingDataBean getHolding(Integer holdingID) throws Exception {
        return getTradeDirect().getHolding(holdingID);
    }

    @Override
    public AccountDataBean getAccountData(String userID) throws Exception {
        return getTradeDirect().getAccountData(userID);
    }

    @Override
    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
        return getTradeDirect().getAccountProfileData(userID);
    }

    @Override
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
        return getTradeDirect().updateAccountProfile(profileData);
    }

    @Override
    public AccountDataBean login(String userID, String password) throws Exception {
        return getTradeDirect().login(userID, password);
    }

    @Override
    public void logout(String userID) throws Exception {
        getTradeDirect().logout(userID);
    }

    @Override
    public AccountDataBean register(String userID, String password, String fullname, String address, String email,
            String creditcard, BigDecimal openBalance) throws Exception {
        return getTradeDirect().register(userID, password, fullname, address, email, creditcard, openBalance);
    }

    @Override
    public OrderDataBean createOrder(AccountDataBean account, QuoteDataBean quote, HoldingDataBean holding,
            String orderType, double quantity) throws Exception {
        return getTradeDirect().createOrder(account, quote, holding, orderType, quantity);
    }

    @Override
    public int getImpl() {
        return TradeConfig.SESSION_TO_DIRECT;
    }

    @Override
    public QuoteDataBean pingTwoPhase(String symbol) throws Exception {
        return getTradeDirect().pingTwoPhase(symbol);
    }

    @Override
    public double investmentReturn(double rnd1, double rnd2) throws Exception {
        return getTradeDirect().investmentReturn(rnd1, rnd2);
    }

    @Override
    public void setInSession(boolean inSession) {
        // no-op for session bean
    }
}
