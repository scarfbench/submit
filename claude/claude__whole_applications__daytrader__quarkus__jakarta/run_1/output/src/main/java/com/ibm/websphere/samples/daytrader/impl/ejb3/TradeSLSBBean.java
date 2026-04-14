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
package com.ibm.websphere.samples.daytrader.impl.ejb3;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import com.ibm.websphere.samples.daytrader.interfaces.RuntimeMode;
import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.interfaces.TradeEJB;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.messaging.MessageProducerService;
import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

import jakarta.enterprise.inject.Default;

@ApplicationScoped
@Default
@TradeEJB
@RuntimeMode("Full EJB3")
@Trace
@Transactional
public class TradeSLSBBean implements TradeServices {

    @Inject
    MessageProducerService messageProducer;

    @PersistenceContext(unitName = "daytrader")
    EntityManager entityManager;

    @Inject
    MarketSummarySingleton marketSummarySingleton;

    @Inject
    AsyncScheduledOrderSubmitter asyncEJBOrderSubmitter;

    @Inject
    RecentQuotePriceChangeList recentQuotePriceChangeList;

    /**
     * Helper to find an AccountDataBean by profile userID via JPQL query.
     * This avoids relying on the inverse @OneToOne(mappedBy) relationship
     * which may not be reliably loaded by EclipseLink on Open Liberty.
     */
    private AccountDataBean findAccountByUserID(String userID) {
        TypedQuery<AccountDataBean> q = entityManager.createQuery(
            "SELECT a FROM accountejb a WHERE a.profile.userID = :uid", AccountDataBean.class);
        q.setParameter("uid", userID);
        List<AccountDataBean> results = q.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public MarketSummaryDataBean getMarketSummary() {
        return marketSummarySingleton.getMarketSummaryDataBean();
    }

    @Override
    @NotNull
    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) {
        OrderDataBean order = null;
        BigDecimal total;
        try {
            AccountDataBean account = findAccountByUserID(userID);
            QuoteDataBean quote = entityManager.find(QuoteDataBean.class, symbol);
            HoldingDataBean holding = null;

            order = createOrder(account, quote, holding, "buy", quantity);

            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).add(orderFee);
            account.setBalance(balance.subtract(total));
            final Integer orderID = order.getOrderID();

            if (orderProcessingMode == TradeConfig.SYNCH) {
                completeOrder(orderID, false);
            } else if (orderProcessingMode == TradeConfig.ASYNCH) {
                completeOrderAsync(orderID, false);
            } else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE) {
                queueOrder(orderID, true);
            }
        } catch (Exception e) {
            Log.error("TradeSLSBBean:buy(" + userID + "," + symbol + "," + quantity + ") --> failed", e);
            throw new RuntimeException(e);
        }
        return order;
    }

    @Override
    @NotNull
    public OrderDataBean sell(final String userID, final Integer holdingID, int orderProcessingMode) {
        OrderDataBean order = null;
        BigDecimal total;
        try {
            AccountDataBean account = findAccountByUserID(userID);

            HoldingDataBean holding = entityManager.find(HoldingDataBean.class, holdingID);

            if (holding == null) {
                Log.debug("TradeSLSBBean:sell User " + userID + " attempted to sell holding " + holdingID + " which has already been sold");
                OrderDataBean orderData = new OrderDataBean();
                orderData.setOrderStatus("cancelled");
                entityManager.persist(orderData);
                return orderData;
            }

            QuoteDataBean quote = holding.getQuote();
            double quantity = holding.getQuantity();

            order = createOrder(account, quote, holding, "sell", quantity);

            holding.setPurchaseDate(new java.sql.Timestamp(0));

            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).subtract(orderFee);
            account.setBalance(balance.add(total));
            final Integer orderID = order.getOrderID();

            if (orderProcessingMode == TradeConfig.SYNCH) {
                completeOrder(orderID, false);
            } else if (orderProcessingMode == TradeConfig.ASYNCH) {
                completeOrderAsync(orderID, false);
            } else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE) {
                queueOrder(orderID, true);
            }
        } catch (Exception e) {
            Log.error("TradeSLSBBean:sell(" + userID + "," + holdingID + ") --> failed", e);
            throw new RuntimeException("TradeSLSBBean:sell(" + userID + "," + holdingID + ")", e);
        }
        return order;
    }

    public void queueOrder(Integer orderID, boolean twoPhase) {
        messageProducer.queueOrderForProcessing(orderID, twoPhase);
    }

    @Override
    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception {
        OrderDataBean order = entityManager.find(OrderDataBean.class, orderID);

        if (order == null) {
            throw new RuntimeException("Error: attempt to complete Order that is null\n" + order);
        }

        order.getQuote();

        if (order.isCompleted()) {
            throw new RuntimeException("Error: attempt to complete Order that is already completed\n" + order);
        }

        AccountDataBean account = order.getAccount();
        QuoteDataBean quote = order.getQuote();
        HoldingDataBean holding = order.getHolding();
        BigDecimal price = order.getPrice();
        double quantity = order.getQuantity();

        if (order.isBuy()) {
            HoldingDataBean newHolding = createHolding(account, quote, quantity, price);
            order.setHolding(newHolding);
            order.setOrderStatus("closed");
            order.setCompletionDate(new java.sql.Timestamp(System.currentTimeMillis()));
            updateQuotePriceVolume(quote.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), quantity);
        }

        if (order.isSell()) {
            if (holding == null) {
                Log.debug("TradeSLSBBean:completeOrder -- Unable to sell order " + order.getOrderID() + " holding already sold");
                order.cancel();
            } else {
                entityManager.remove(holding);
                order.setHolding(null);
                order.setOrderStatus("closed");
                order.setCompletionDate(new java.sql.Timestamp(System.currentTimeMillis()));
                updateQuotePriceVolume(quote.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), quantity);
            }
        }

        Log.trace("TradeSLSBBean:completeOrder--> Completed Order " + order.getOrderID() + "\n\t Order info: " + order + "\n\t Account info: " + account
            + "\n\t Quote info: " + quote + "\n\t Holding info: " + holding);

        return order;
    }

    @Override
    public Future<OrderDataBean> completeOrderAsync(Integer orderID, boolean twoPhase) throws Exception {
        asyncEJBOrderSubmitter.submitOrder(orderID, twoPhase);
        return null;
    }

    @Override
    public void cancelOrder(Integer orderID, boolean twoPhase) {
        OrderDataBean order = entityManager.find(OrderDataBean.class, orderID);
        order.cancel();
    }

    @Override
    public void orderCompleted(String userID, Integer orderID) {
        throw new UnsupportedOperationException("TradeSLSBBean:orderCompleted method not supported");
    }

    @Override
    public Collection<OrderDataBean> getOrders(String userID) {
        TypedQuery<OrderDataBean> query = entityManager.createQuery(
            "SELECT o FROM orderejb o WHERE o.account.profile.userID = :userID ORDER BY o.openDate DESC",
            OrderDataBean.class);
        query.setParameter("userID", userID);
        return query.getResultList();
    }

    @Override
    public Collection<OrderDataBean> getClosedOrders(String userID) {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<OrderDataBean> criteriaQuery = criteriaBuilder.createQuery(OrderDataBean.class);
            Root<OrderDataBean> orders = criteriaQuery.from(OrderDataBean.class);
            criteriaQuery.select(orders);
            criteriaQuery.where(
                    criteriaBuilder.equal(orders.get("orderStatus"),
                            criteriaBuilder.parameter(String.class, "p_status")),
                    criteriaBuilder.equal(orders.get("account").get("profile").get("userID"),
                            criteriaBuilder.parameter(String.class, "p_userid")));

            TypedQuery<OrderDataBean> q = entityManager.createQuery(criteriaQuery);
            q.setParameter("p_status", "closed");
            q.setParameter("p_userid", userID);
            List<OrderDataBean> results = q.getResultList();

            Iterator<OrderDataBean> itr = results.iterator();
            while (itr.hasNext()) {
                OrderDataBean order = itr.next();
                if (TradeConfig.getLongRun()) {
                    entityManager.remove(order);
                } else {
                    order.setOrderStatus("completed");
                }
            }
            return results;
        } catch (Exception e) {
            Log.error("TradeSLSBBean.getClosedOrders", e);
            throw new RuntimeException("TradeSLSBBean.getClosedOrders - error", e);
        }
    }

    @Override
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) {
        try {
            QuoteDataBean quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
            entityManager.persist(quote);
            Log.trace("TradeSLSBBean:createQuote-->" + quote);
            return quote;
        } catch (Exception e) {
            Log.error("TradeSLSBBean:createQuote -- exception creating Quote", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public QuoteDataBean getQuote(String symbol) {
        return entityManager.find(QuoteDataBean.class, symbol);
    }

    @Override
    public Collection<QuoteDataBean> getAllQuotes() {
        TypedQuery<QuoteDataBean> query = entityManager.createNamedQuery("quoteejb.allQuotes", QuoteDataBean.class);
        return query.getResultList();
    }

    @Override
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) {
        if (!TradeConfig.getUpdateQuotePrices()) {
            return new QuoteDataBean();
        }

        Log.trace("TradeSLSBBean:updateQuote", symbol, changeFactor);

        TypedQuery<QuoteDataBean> q = entityManager.createNamedQuery("quoteejb.quoteForUpdate", QuoteDataBean.class);
        q.setParameter(1, symbol);
        QuoteDataBean quote = q.getSingleResult();

        BigDecimal oldPrice = quote.getPrice();
        BigDecimal openPrice = quote.getOpen();

        if (oldPrice.equals(TradeConfig.PENNY_STOCK_PRICE)) {
            changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
        } else if (oldPrice.compareTo(TradeConfig.MAXIMUM_STOCK_PRICE) > 0) {
            changeFactor = TradeConfig.MAXIMUM_STOCK_SPLIT_MULTIPLIER;
        }

        BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, BigDecimal.ROUND_HALF_UP);

        quote.setPrice(newPrice);
        quote.setChange(newPrice.subtract(openPrice).doubleValue());
        quote.setVolume(quote.getVolume() + sharesTraded);
        entityManager.merge(quote);

        if (TradeConfig.getPublishQuotePriceChange()) {
            publishQuotePriceChange(quote, oldPrice, changeFactor, sharesTraded);
        }

        recentQuotePriceChangeList.add(quote);

        return quote;
    }

    @Override
    public Collection<@NotNull HoldingDataBean> getHoldings(String userID) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HoldingDataBean> criteriaQuery = criteriaBuilder.createQuery(HoldingDataBean.class);
        Root<HoldingDataBean> holdings = criteriaQuery.from(HoldingDataBean.class);
        criteriaQuery.where(
                criteriaBuilder.equal(holdings.get("account").get("profile").get("userID"),
                        criteriaBuilder.parameter(String.class, "p_userid")));
        criteriaQuery.select(holdings);

        TypedQuery<HoldingDataBean> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setParameter("p_userid", userID);
        return typedQuery.getResultList();
    }

    @Override
    public HoldingDataBean getHolding(Integer holdingID) {
        return entityManager.find(HoldingDataBean.class, holdingID);
    }

    @Override
    public AccountDataBean getAccountData(String userID) {
        AccountDataBean account = findAccountByUserID(userID);
        if (account != null) {
            account.setProfileID(userID);
        }
        return account;
    }

    @Override
    public AccountProfileDataBean getAccountProfileData(String userID) {
        return entityManager.find(AccountProfileDataBean.class, userID);
    }

    @Override
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) {
        AccountProfileDataBean temp = entityManager.find(AccountProfileDataBean.class, profileData.getUserID());
        temp.setAddress(profileData.getAddress());
        temp.setPassword(profileData.getPassword());
        temp.setFullName(profileData.getFullName());
        temp.setCreditCard(profileData.getCreditCard());
        temp.setEmail(profileData.getEmail());
        entityManager.merge(temp);
        return temp;
    }

    @Override
    public AccountDataBean login(String userID, String password) throws RollbackException {
        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        if (profile == null) {
            throw new RuntimeException("No such user: " + userID);
        }
        AccountDataBean account = findAccountByUserID(userID);
        if (account == null) {
            throw new RuntimeException("No account found for user: " + userID);
        }
        account.login(password);
        Log.trace("TradeSLSBBean:login(" + userID + "," + password + ") success" + account);
        return account;
    }

    @Override
    public void logout(String userID) {
        AccountDataBean account = findAccountByUserID(userID);
        if (account != null) {
            account.logout();
        }
        Log.trace("TradeSLSBBean:logout(" + userID + ") success");
    }

    @Override
    public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditcard, BigDecimal openBalance) {
        AccountDataBean account = null;
        AccountProfileDataBean profile = null;

        profile = entityManager.find(AccountProfileDataBean.class, userID);

        if (profile != null) {
            Log.error("Failed to register new Account - AccountProfile with userID(" + userID + ") already exists");
            return null;
        } else {
            profile = new AccountProfileDataBean(userID, password, fullname, address, email, creditcard);
            account = new AccountDataBean(0, 0, null, new Timestamp(System.currentTimeMillis()), openBalance, openBalance, userID);

            profile.setAccount(account);
            account.setProfile(profile);

            entityManager.persist(profile);
            entityManager.persist(account);
        }

        return account;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void publishQuotePriceChange(QuoteDataBean quote, BigDecimal oldPrice, BigDecimal changeFactor, double sharesTraded) {
        if (!TradeConfig.getPublishQuotePriceChange()) {
            return;
        }
        messageProducer.publishQuotePriceChange(
            quote.getSymbol(),
            quote.getCompanyName(),
            quote.getPrice(),
            oldPrice,
            quote.getOpen(),
            quote.getLow(),
            quote.getHigh(),
            quote.getVolume(),
            changeFactor,
            sharesTraded
        );
    }

    public OrderDataBean createOrder(AccountDataBean account, QuoteDataBean quote, HoldingDataBean holding, String orderType, double quantity) {
        OrderDataBean order;
        try {
            order = new OrderDataBean(orderType, "open", new Timestamp(System.currentTimeMillis()), null, quantity, quote.getPrice().setScale(
                    FinancialUtils.SCALE, FinancialUtils.ROUND), TradeConfig.getOrderFee(orderType), account, quote, holding);
            entityManager.persist(order);
        } catch (Exception e) {
            Log.error("TradeSLSBBean:createOrder -- failed to create Order.", e);
            throw new RuntimeException("TradeSLSBBean:createOrder -- failed to create Order.", e);
        }
        return order;
    }

    private HoldingDataBean createHolding(AccountDataBean account, QuoteDataBean quote, double quantity, BigDecimal purchasePrice) throws Exception {
        HoldingDataBean newHolding = new HoldingDataBean(quantity, purchasePrice, new Timestamp(System.currentTimeMillis()), account, quote);
        entityManager.persist(newHolding);
        return newHolding;
    }

    @Override
    public double investmentReturn(double investment, double NetValue) throws Exception {
        double diff = NetValue - investment;
        double ir = diff / investment;
        return ir;
    }

    @Override
    public QuoteDataBean pingTwoPhase(String symbol) throws Exception {
        QuoteDataBean quoteData = null;
        try {
            quoteData = entityManager.find(QuoteDataBean.class, symbol);
            double sharesTraded = (Math.random() * 100) + 1;
            BigDecimal oldPrice = quoteData.getPrice();
            BigDecimal openPrice = quoteData.getOpen();
            BigDecimal changeFactor = new BigDecimal(Math.random() * 100);
            BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            quoteData.setPrice(newPrice);
            quoteData.setChange(newPrice.subtract(openPrice).doubleValue());
            quoteData.setVolume(quoteData.getVolume() + sharesTraded);
            entityManager.merge(quoteData);
            messageProducer.sendBrokerPing("Ping message sent from TradeSLSBBean:pingTwoPhase at " + new java.util.Date());
        } catch (Exception e) {
            Log.error("TradeSLSBBean:pingTwoPhase -- exception caught", e);
        }
        return quoteData;
    }

    class quotePriceComparator implements Comparator<QuoteDataBean> {
        @Override
        public int compare(QuoteDataBean quote1, QuoteDataBean quote2) {
            double change1 = quote1.getChange();
            double change2 = quote2.getChange();
            return new Double(change2).compareTo(change1);
        }
    }

    @Override
    public int getImpl() {
        return TradeConfig.EJB3;
    }

    @Override
    public void setInSession(boolean inSession) {
        throw new UnsupportedOperationException("TradeSLSBBean::setInGlobalTxn not supported");
    }
}
