package com.ibm.websphere.samples.daytrader.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@ApplicationScoped
public class TradeServiceImpl implements TradeServices {

    @Inject
    EntityManager em;

    /**
     * Helper to get account by userID using JPQL query.
     * This avoids relying on the lazy inverse-side of the OneToOne relationship.
     */
    private AccountDataBean getAccountByUserID(String userID) {
        List<AccountDataBean> results = em.createQuery(
                "SELECT a FROM accountejb a WHERE a.profile.userID = :userID", AccountDataBean.class)
                .setParameter("userID", userID)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional
    public MarketSummaryDataBean getMarketSummary() throws Exception {
        List<QuoteDataBean> allQuotes = new ArrayList<>(em.createNamedQuery("quoteejb.allQuotes", QuoteDataBean.class).getResultList());

        BigDecimal TSIA = BigDecimal.ZERO;
        BigDecimal openTSIA = BigDecimal.ZERO;
        double totalVolume = 0.0;

        int count = 0;
        for (QuoteDataBean quote : allQuotes) {
            if (quote.getPrice() != null) {
                TSIA = TSIA.add(quote.getPrice());
                count++;
            }
            if (quote.getOpen() != null) {
                openTSIA = openTSIA.add(quote.getOpen());
            }
            totalVolume += quote.getVolume();
        }

        if (count > 0) {
            TSIA = TSIA.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
            openTSIA = openTSIA.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
        }

        // Top 5 gainers and losers
        allQuotes.sort(Comparator.comparingDouble(QuoteDataBean::getChange).reversed());
        List<QuoteDataBean> topGainers = new ArrayList<>(allQuotes.subList(0, Math.min(5, allQuotes.size())));

        allQuotes.sort(Comparator.comparingDouble(QuoteDataBean::getChange));
        List<QuoteDataBean> topLosers = new ArrayList<>(allQuotes.subList(0, Math.min(5, allQuotes.size())));

        return new MarketSummaryDataBean(TSIA, openTSIA, totalVolume,
                new ArrayList<>(topGainers), new ArrayList<>(topLosers));
    }

    @Override
    @Transactional
    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
        AccountDataBean account = getAccountByUserID(userID);
        if (account == null) throw new RuntimeException("No account found for " + userID);

        QuoteDataBean quote = em.find(QuoteDataBean.class, symbol);
        if (quote == null) throw new RuntimeException("No quote found for " + symbol);

        BigDecimal price = quote.getPrice();
        BigDecimal orderFee = TradeConfig.getOrderFee(quote.getSymbol());

        // Create the order
        OrderDataBean order = new OrderDataBean("buy", "open",
                new Timestamp(System.currentTimeMillis()), null,
                quantity, price, orderFee, account, quote, null);
        em.persist(order);

        // Update quote volume
        quote.setVolume(quote.getVolume() + quantity);

        // Complete the order immediately (synchronous mode)
        return completeOrderInternal(order, account, quote);
    }

    @Override
    @Transactional
    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
        AccountDataBean account = getAccountByUserID(userID);
        if (account == null) throw new RuntimeException("No account found for " + userID);

        HoldingDataBean holding = em.find(HoldingDataBean.class, holdingID);
        if (holding == null) throw new RuntimeException("No holding found for " + holdingID);

        QuoteDataBean quote = holding.getQuote();
        if (quote == null) throw new RuntimeException("No quote found for holding " + holdingID);

        double quantity = holding.getQuantity();
        BigDecimal price = quote.getPrice();
        BigDecimal orderFee = TradeConfig.getOrderFee(quote.getSymbol());

        OrderDataBean order = new OrderDataBean("sell", "open",
                new Timestamp(System.currentTimeMillis()), null,
                quantity, price, orderFee, account, quote, holding);
        em.persist(order);

        // Update quote volume
        quote.setVolume(quote.getVolume() + quantity);

        // Complete the order immediately (synchronous mode)
        return completeOrderInternal(order, account, quote);
    }

    private OrderDataBean completeOrderInternal(OrderDataBean order, AccountDataBean account, QuoteDataBean quote) {
        order.setOrderStatus("closed");
        order.setCompletionDate(new Timestamp(System.currentTimeMillis()));

        BigDecimal price = order.getPrice();
        double quantity = order.getQuantity();
        BigDecimal orderFee = order.getOrderFee();

        if (order.isBuy()) {
            // Create new holding
            HoldingDataBean holding = new HoldingDataBean(quantity, price,
                    new Timestamp(System.currentTimeMillis()), account, quote);
            em.persist(holding);
            order.setHolding(holding);

            // Debit account
            BigDecimal total = (new BigDecimal(quantity)).multiply(price).add(orderFee);
            account.setBalance(account.getBalance().subtract(total));
        } else if (order.isSell()) {
            // Credit account
            BigDecimal total = (new BigDecimal(quantity)).multiply(price).subtract(orderFee);
            account.setBalance(account.getBalance().add(total));

            // Remove holding - first null out all FK references to it
            HoldingDataBean holding = order.getHolding();
            if (holding != null) {
                // Null out holding references in all orders to avoid FK constraint violation
                em.createQuery("UPDATE orderejb o SET o.holding = NULL WHERE o.holding.holdingID = :holdingId")
                        .setParameter("holdingId", holding.getHoldingID())
                        .executeUpdate();
                em.flush();
                em.remove(em.merge(holding));
            }
        }

        // Update quote price if configured
        if (TradeConfig.getUpdateQuotePrices()) {
            updateQuotePriceVolumeInternal(quote);
        }

        return order;
    }

    private void updateQuotePriceVolumeInternal(QuoteDataBean quote) {
        BigDecimal changeFactor = TradeConfig.getRandomPriceChangeFactor();
        BigDecimal newPrice = quote.getPrice().multiply(changeFactor).setScale(2, RoundingMode.HALF_UP);

        if (newPrice.compareTo(TradeConfig.PENNY_STOCK_PRICE) <= 0) {
            newPrice = newPrice.multiply(TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        }
        if (newPrice.compareTo(TradeConfig.MAXIMUM_STOCK_PRICE) >= 0) {
            newPrice = newPrice.multiply(TradeConfig.MAXIMUM_STOCK_SPLIT_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        }

        quote.setPrice(newPrice);
        quote.setChange(newPrice.subtract(quote.getOpen()).doubleValue());
        quote.setVolume(quote.getVolume() + 1);
    }

    @Override
    @Transactional
    public OrderDataBean completeOrder(Integer orderID) throws Exception {
        OrderDataBean order = em.find(OrderDataBean.class, orderID);
        if (order == null) throw new RuntimeException("No order found for " + orderID);
        if (!order.isOpen()) return order;

        AccountDataBean account = order.getAccount();
        QuoteDataBean quote = order.getQuote();
        return completeOrderInternal(order, account, quote);
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderID) throws Exception {
        OrderDataBean order = em.find(OrderDataBean.class, orderID);
        if (order != null) {
            order.cancel();
        }
    }

    @Override
    @Transactional
    public OrderDataBean getOrderData(int orderID) throws Exception {
        return em.find(OrderDataBean.class, orderID);
    }

    @Override
    @Transactional
    public Collection<OrderDataBean> getOrders(String userID) throws Exception {
        AccountDataBean account = getAccountByUserID(userID);
        if (account == null) return new ArrayList<>();

        return em.createQuery("SELECT o FROM orderejb o WHERE o.account.accountID = :accountId ORDER BY o.openDate DESC", OrderDataBean.class)
                .setParameter("accountId", account.getAccountID())
                .getResultList();
    }

    @Override
    @Transactional
    public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception {
        TypedQuery<OrderDataBean> query = em.createNamedQuery("orderejb.closedOrders", OrderDataBean.class);
        query.setParameter("userID", userID);
        List<OrderDataBean> closedOrders = query.getResultList();

        // Mark them as completed
        em.createNamedQuery("orderejb.completeClosedOrders")
            .setParameter("userID", userID)
            .executeUpdate();

        return closedOrders;
    }

    @Override
    @Transactional
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
        QuoteDataBean quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
        em.persist(quote);
        return quote;
    }

    @Override
    @Transactional
    public QuoteDataBean getQuote(String symbol) throws Exception {
        return em.find(QuoteDataBean.class, symbol);
    }

    @Override
    @Transactional
    public Collection<QuoteDataBean> getAllQuotes() throws Exception {
        return em.createNamedQuery("quoteejb.allQuotes", QuoteDataBean.class).getResultList();
    }

    @Override
    @Transactional
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) throws Exception {
        QuoteDataBean quote = em.find(QuoteDataBean.class, symbol);
        if (quote == null) throw new RuntimeException("No quote found for " + symbol);

        BigDecimal newPrice = quote.getPrice().multiply(changeFactor).setScale(2, RoundingMode.HALF_UP);
        quote.setPrice(newPrice);
        quote.setVolume(quote.getVolume() + sharesTraded);
        quote.setChange(newPrice.subtract(quote.getOpen()).doubleValue());

        return quote;
    }

    @Override
    @Transactional
    public Collection<HoldingDataBean> getHoldings(String userID) throws Exception {
        AccountDataBean account = getAccountByUserID(userID);
        if (account == null) return new ArrayList<>();

        return em.createQuery("SELECT h FROM holdingejb h WHERE h.account.accountID = :accountId", HoldingDataBean.class)
                .setParameter("accountId", account.getAccountID())
                .getResultList();
    }

    @Override
    @Transactional
    public HoldingDataBean getHolding(Integer holdingID) throws Exception {
        return em.find(HoldingDataBean.class, holdingID);
    }

    @Override
    @Transactional
    public AccountDataBean getAccountData(String userID) throws Exception {
        return getAccountByUserID(userID);
    }

    @Override
    @Transactional
    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
        return em.find(AccountProfileDataBean.class, userID);
    }

    @Override
    @Transactional
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
        AccountProfileDataBean existing = em.find(AccountProfileDataBean.class, profileData.getUserID());
        if (existing == null) throw new RuntimeException("No profile found for " + profileData.getUserID());

        existing.setPassword(profileData.getPassword());
        existing.setFullName(profileData.getFullName());
        existing.setAddress(profileData.getAddress());
        existing.setEmail(profileData.getEmail());
        existing.setCreditCard(profileData.getCreditCard());

        return existing;
    }

    @Override
    @Transactional
    public AccountDataBean login(String userID, String password) throws Exception {
        AccountProfileDataBean profile = em.find(AccountProfileDataBean.class, userID);
        if (profile == null) throw new RuntimeException("No profile found for " + userID);

        AccountDataBean account = getAccountByUserID(userID);
        if (account == null) throw new RuntimeException("No account found for " + userID);

        // Verify password directly since account.login uses lazy profile
        if (!profile.getPassword().equals(password)) {
            throw new RuntimeException("Login failure: incorrect password for " + userID);
        }

        account.setLastLogin(new Timestamp(System.currentTimeMillis()));
        account.setLoginCount(account.getLoginCount() + 1);
        return account;
    }

    @Override
    @Transactional
    public void logout(String userID) throws Exception {
        AccountDataBean account = getAccountByUserID(userID);
        if (account != null) {
            account.setLogoutCount(account.getLogoutCount() + 1);
        }
    }

    @Override
    @Transactional
    public AccountDataBean register(String userID, String password, String fullname, String address,
            String email, String creditcard, BigDecimal openBalance) throws Exception {

        AccountProfileDataBean profile = new AccountProfileDataBean(userID, password, fullname, address, email, creditcard);
        em.persist(profile);

        AccountDataBean account = new AccountDataBean(0, 0,
                new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()),
                openBalance, openBalance, userID);
        account.setProfile(profile);
        em.persist(account);

        return account;
    }

    @Override
    @Transactional
    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
        RunStatsDataBean stats = new RunStatsDataBean();

        // Gather stats before deleting
        Long userCount = em.createQuery("SELECT COUNT(a) FROM accountejb a", Long.class).getSingleResult();
        Long quoteCount = em.createQuery("SELECT COUNT(q) FROM quoteejb q", Long.class).getSingleResult();
        Long orderCount = em.createQuery("SELECT COUNT(o) FROM orderejb o", Long.class).getSingleResult();
        Long holdingCount = em.createQuery("SELECT COUNT(h) FROM holdingejb h", Long.class).getSingleResult();

        stats.setTradeUserCount(userCount.intValue());
        stats.setTradeStockCount(quoteCount.intValue());
        stats.setOrderCount(orderCount.intValue());
        stats.setHoldingCount(holdingCount.intValue());

        if (deleteAll) {
            em.createQuery("DELETE FROM orderejb").executeUpdate();
            em.createQuery("DELETE FROM holdingejb").executeUpdate();
            em.createQuery("DELETE FROM accountejb").executeUpdate();
            em.createQuery("DELETE FROM accountprofileejb").executeUpdate();
            em.createQuery("DELETE FROM quoteejb").executeUpdate();
        }

        return stats;
    }

    @Transactional
    public void populateDatabase(int maxUsers, int maxQuotes) throws Exception {
        Log.log("Populating database with " + maxQuotes + " quotes and " + maxUsers + " users...");

        // Create quotes
        for (int i = 0; i < maxQuotes; i++) {
            String symbol = "s:" + i;
            String companyName = "S" + i + " Incorporated";
            BigDecimal price = TradeConfig.rndBigDecimal(1000.0f);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                price = new BigDecimal("10.00");
            }
            QuoteDataBean quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
            em.persist(quote);

            if (i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
        em.flush();
        em.clear();

        Log.log("Created " + maxQuotes + " quotes");

        // Create users
        for (int i = 0; i < maxUsers; i++) {
            String userID = "uid:" + i;
            String password = "xxx";
            String fullname = TradeConfig.rndFullName();
            String address = TradeConfig.rndAddress();
            String email = TradeConfig.rndEmail(userID);
            String creditcard = TradeConfig.rndCreditCard();
            BigDecimal openBalance = new BigDecimal(TradeConfig.rndBalance());

            AccountProfileDataBean profile = new AccountProfileDataBean(userID, password, fullname, address, email, creditcard);
            em.persist(profile);

            AccountDataBean account = new AccountDataBean(0, 0,
                    new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()),
                    openBalance, openBalance, userID);
            account.setProfile(profile);
            em.persist(account);

            // Give each user some random holdings
            int numHoldings = TradeConfig.rndInt(TradeConfig.getMAX_HOLDINGS()) + 1;
            for (int j = 0; j < numHoldings; j++) {
                String symbol = TradeConfig.rndSymbol();
                QuoteDataBean quote = em.find(QuoteDataBean.class, symbol);
                if (quote != null) {
                    double quantity = TradeConfig.rndQuantity();
                    BigDecimal purchasePrice = quote.getPrice();
                    HoldingDataBean holding = new HoldingDataBean(quantity, purchasePrice,
                            new Timestamp(System.currentTimeMillis()), account, quote);
                    em.persist(holding);
                }
            }

            if (i % 50 == 0) {
                em.flush();
                em.clear();
            }
        }
        em.flush();
        em.clear();

        Log.log("Created " + maxUsers + " users with holdings");
        Log.log("Database population complete");
    }
}
