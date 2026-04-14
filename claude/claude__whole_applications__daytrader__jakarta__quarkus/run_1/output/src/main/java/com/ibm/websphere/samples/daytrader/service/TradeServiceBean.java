/**
 * Quarkus CDI bean replacing TradeSLSBBean (EJB Stateless Session Bean).
 * This is the primary implementation of TradeServices using JPA EntityManager.
 */
package com.ibm.websphere.samples.daytrader.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@ApplicationScoped
@Transactional
public class TradeServiceBean implements TradeServices {

    @Inject
    EntityManager entityManager;

    @Inject
    MarketSummaryService marketSummaryService;

    @Inject
    RecentQuotePriceChangeList recentQuotePriceChangeList;

    @Override
    public MarketSummaryDataBean getMarketSummary() {
        return marketSummaryService.getMarketSummaryDataBean();
    }

    @Override
    @NotNull
    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) {
        OrderDataBean order = null;
        BigDecimal total;
        try {
            AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
            AccountDataBean account = profile.getAccount();
            QuoteDataBean quote = entityManager.find(QuoteDataBean.class, symbol);
            HoldingDataBean holding = null;

            order = createOrder(account, quote, holding, "buy", quantity);

            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).add(orderFee);
            account.setBalance(balance.subtract(total));
            final Integer orderID = order.getOrderID();

            // Always use synchronous processing in Quarkus (no JMS)
            completeOrder(orderID, false);
        } catch (Exception e) {
            Log.error("TradeServiceBean:buy(" + userID + "," + symbol + "," + quantity + ") --> failed", e);
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
            AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
            AccountDataBean account = profile.getAccount();

            HoldingDataBean holding = entityManager.find(HoldingDataBean.class, holdingID);

            if (holding == null) {
                Log.debug("TradeServiceBean:sell User " + userID + " attempted to sell holding " + holdingID + " which has already been sold");
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

            // Always use synchronous processing in Quarkus (no JMS)
            completeOrder(orderID, false);
        } catch (Exception e) {
            Log.error("TradeServiceBean:sell(" + userID + "," + holdingID + ") --> failed", e);
            throw new RuntimeException("TradeServiceBean:sell(" + userID + "," + holdingID + ")", e);
        }
        return order;
    }

    @Override
    public void queueOrder(Integer orderID, boolean twoPhase) throws Exception {
        // JMS removed in Quarkus migration - process synchronously
        completeOrder(orderID, twoPhase);
    }

    @Override
    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception {
        OrderDataBean order = entityManager.find(OrderDataBean.class, orderID);

        if (order == null) {
            throw new RuntimeException("Error: attempt to complete Order that is null");
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
                Log.debug("TradeServiceBean:completeOrder -- Unable to sell order " + order.getOrderID() + " holding already sold");
                order.cancel();
            } else {
                entityManager.remove(holding);
                order.setHolding(null);
                order.setOrderStatus("closed");
                order.setCompletionDate(new java.sql.Timestamp(System.currentTimeMillis()));
                updateQuotePriceVolume(quote.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), quantity);
            }
        }

        Log.trace("TradeServiceBean:completeOrder--> Completed Order " + order.getOrderID());
        return order;
    }

    @Override
    public Future<OrderDataBean> completeOrderAsync(Integer orderID, boolean twoPhase) throws Exception {
        // Async removed in Quarkus migration - process synchronously
        completeOrder(orderID, twoPhase);
        return null;
    }

    @Override
    public void cancelOrder(Integer orderID, boolean twoPhase) {
        OrderDataBean order = entityManager.find(OrderDataBean.class, orderID);
        order.cancel();
    }

    @Override
    public void orderCompleted(String userID, Integer orderID) {
        throw new UnsupportedOperationException("TradeServiceBean:orderCompleted method not supported");
    }

    @Override
    public Collection<OrderDataBean> getOrders(String userID) {
        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        AccountDataBean account = profile.getAccount();
        // Force initialization of lazy collection before transaction closes
        Collection<OrderDataBean> orders = account.getOrders();
        if (orders != null) {
            orders.size(); // trigger lazy load
        }
        return orders != null ? new java.util.ArrayList<>(orders) : new java.util.ArrayList<>();
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
            Log.error("TradeServiceBean.getClosedOrders", e);
            throw new RuntimeException("TradeServiceBean.getClosedOrders - error", e);
        }
    }

    @Override
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) {
        try {
            QuoteDataBean quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
            entityManager.persist(quote);
            Log.trace("TradeServiceBean:createQuote-->" + quote);
            return quote;
        } catch (Exception e) {
            Log.error("TradeServiceBean:createQuote -- exception creating Quote", e);
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

        Log.trace("TradeServiceBean:updateQuote", symbol, changeFactor);

        TypedQuery<QuoteDataBean> q = entityManager.createNamedQuery("quoteejb.quoteForUpdate", QuoteDataBean.class);
        q.setParameter("symbol", symbol);
        QuoteDataBean quote = q.getSingleResult();

        BigDecimal oldPrice = quote.getPrice();
        BigDecimal openPrice = quote.getOpen();

        if (oldPrice.equals(TradeConfig.PENNY_STOCK_PRICE)) {
            changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
        } else if (oldPrice.compareTo(TradeConfig.MAXIMUM_STOCK_PRICE) > 0) {
            changeFactor = TradeConfig.MAXIMUM_STOCK_SPLIT_MULTIPLIER;
        }

        BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, RoundingMode.HALF_UP);

        quote.setPrice(newPrice);
        quote.setChange(newPrice.subtract(openPrice).doubleValue());
        quote.setVolume(quote.getVolume() + sharesTraded);
        entityManager.merge(quote);

        recentQuotePriceChangeList.add(quote);

        return quote;
    }

    @Override
    public Collection<HoldingDataBean> getHoldings(String userID) {
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
        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        if (profile == null) {
            return null;
        }
        AccountDataBean account = profile.getAccount();
        account.setProfileID(profile.getUserID());
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
    public AccountDataBean login(String userID, String password) throws Exception {
        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        if (profile == null) {
            throw new RuntimeException("No such user: " + userID);
        }

        AccountDataBean account = profile.getAccount();
        account.login(password);

        Log.trace("TradeServiceBean:login(" + userID + "," + password + ") success" + account);
        return account;
    }

    @Override
    public void logout(String userID) {
        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        AccountDataBean account = profile.getAccount();
        account.logout();
        Log.trace("TradeServiceBean:logout(" + userID + ") success");
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

    @Override
    public OrderDataBean createOrder(AccountDataBean account, QuoteDataBean quote, HoldingDataBean holding, String orderType, double quantity) {
        OrderDataBean order;
        try {
            order = new OrderDataBean(orderType, "open", new Timestamp(System.currentTimeMillis()), null, quantity, quote.getPrice().setScale(
                    FinancialUtils.SCALE, FinancialUtils.ROUND), TradeConfig.getOrderFee(orderType), account, quote, holding);
            entityManager.persist(order);
        } catch (Exception e) {
            Log.error("TradeServiceBean:createOrder -- failed to create Order.", e);
            throw new RuntimeException("TradeServiceBean:createOrder -- failed to create Order.", e);
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
        // JMS removed - just return the quote
        return entityManager.find(QuoteDataBean.class, symbol);
    }

    @Override
    public int getImpl() {
        return TradeConfig.EJB3;
    }

    @Override
    public void setInSession(boolean inSession) {
        // No-op in Quarkus
    }
}
