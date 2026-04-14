package com.ibm.websphere.samples.daytrader.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.repositories.AccountProfileRepository;
import com.ibm.websphere.samples.daytrader.repositories.AccountRepository;
import com.ibm.websphere.samples.daytrader.repositories.HoldingRepository;
import com.ibm.websphere.samples.daytrader.repositories.OrderRepository;
import com.ibm.websphere.samples.daytrader.repositories.QuoteRepository;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Service
@Transactional
public class TradeService {

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

    public MarketSummaryDataBean getMarketSummary() throws Exception {
        try {
            List<QuoteDataBean> quotes = quoteRepository.findAllOrderByChangeDesc();

            Collection<QuoteDataBean> topGainers = new ArrayList<>();
            Collection<QuoteDataBean> topLosers = new ArrayList<>();

            BigDecimal TSIA = FinancialUtils.ZERO;
            BigDecimal openTSIA = FinancialUtils.ZERO;
            double volume = 0.0;

            if (quotes.size() > 5) {
                for (int i = 0; i < 5; i++) {
                    topGainers.add(quotes.get(i));
                }
                for (int i = quotes.size() - 1; i >= quotes.size() - 5; i--) {
                    topLosers.add(quotes.get(i));
                }
                for (QuoteDataBean quote : quotes) {
                    BigDecimal price = quote.getPrice();
                    BigDecimal open = quote.getOpen();
                    double vol = quote.getVolume();
                    TSIA = TSIA.add(price);
                    openTSIA = openTSIA.add(open);
                    volume += vol;
                }
                TSIA = TSIA.divide(new BigDecimal(quotes.size()), FinancialUtils.SCALE, FinancialUtils.ROUND);
                openTSIA = openTSIA.divide(new BigDecimal(quotes.size()), FinancialUtils.SCALE, FinancialUtils.ROUND);
            }

            return new MarketSummaryDataBean(TSIA, openTSIA, volume, topGainers, topLosers);
        } catch (Exception e) {
            Log.error("TradeService:getMarketSummary -- error", e);
            throw e;
        }
    }

    @Transactional
    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
        try {
            AccountProfileDataBean profile = accountProfileRepository.findById(userID)
                .orElseThrow(() -> new RuntimeException("No such user: " + userID));
            AccountDataBean account = profile.getAccount();
            QuoteDataBean quote = quoteRepository.findById(symbol)
                .orElseThrow(() -> new RuntimeException("No such quote: " + symbol));

            OrderDataBean order = createOrder(account, quote, null, "buy", quantity);

            // Debit account
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal total = (new BigDecimal(quantity).multiply(price)).add(orderFee);
            account.setBalance(account.getBalance().subtract(total));
            accountRepository.save(account);

            // Complete order synchronously
            completeOrder(order.getOrderID(), false);

            return order;
        } catch (Exception e) {
            Log.error("TradeService:buy(" + userID + "," + symbol + "," + quantity + ") --> failed", e);
            throw e;
        }
    }

    @Transactional
    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
        try {
            AccountProfileDataBean profile = accountProfileRepository.findById(userID)
                .orElseThrow(() -> new RuntimeException("No such user: " + userID));
            AccountDataBean account = profile.getAccount();

            HoldingDataBean holding = holdingRepository.findById(holdingID).orElse(null);
            if (holding == null) {
                Log.debug("TradeService:sell User " + userID + " attempted to sell holding " + holdingID + " which has already been sold");
                OrderDataBean cancelledOrder = new OrderDataBean();
                cancelledOrder.setOrderType("sell");
                cancelledOrder.setOrderStatus("cancelled");
                cancelledOrder.setOpenDate(new Timestamp(System.currentTimeMillis()));
                cancelledOrder.setQuantity(0);
                cancelledOrder.setOrderFee(TradeConfig.getOrderFee("sell"));
                cancelledOrder.setAccount(account);
                orderRepository.save(cancelledOrder);
                return cancelledOrder;
            }

            QuoteDataBean quote = holding.getQuote();
            double quantity = holding.getQuantity();

            OrderDataBean order = createOrder(account, quote, holding, "sell", quantity);

            // Mark holding as in-flight
            holding.setPurchaseDate(new java.sql.Timestamp(0));
            holdingRepository.save(holding);

            // Credit account
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal total = (new BigDecimal(quantity).multiply(price)).subtract(orderFee);
            account.setBalance(account.getBalance().add(total));
            accountRepository.save(account);

            // Complete order synchronously
            completeOrder(order.getOrderID(), false);

            return order;
        } catch (Exception e) {
            Log.error("TradeService:sell(" + userID + "," + holdingID + ") --> failed", e);
            throw e;
        }
    }

    @Transactional
    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception {
        OrderDataBean order = orderRepository.findById(orderID)
            .orElseThrow(() -> new RuntimeException("Error: attempt to complete Order that is null, orderID=" + orderID));

        if (order.isCompleted()) {
            throw new RuntimeException("Error: attempt to complete Order that is already completed\n" + order);
        }

        AccountDataBean account = order.getAccount();
        QuoteDataBean quote = order.getQuote();
        HoldingDataBean holding = order.getHolding();
        BigDecimal price = order.getPrice();
        double quantity = order.getQuantity();

        if (order.isBuy()) {
            HoldingDataBean newHolding = new HoldingDataBean(quantity, price,
                new Timestamp(System.currentTimeMillis()), account, quote);
            holdingRepository.save(newHolding);
            order.setHolding(newHolding);
            order.setOrderStatus("closed");
            order.setCompletionDate(new Timestamp(System.currentTimeMillis()));
            updateQuotePriceVolume(quote.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), quantity);
        }

        if (order.isSell()) {
            if (holding == null) {
                Log.debug("TradeService:completeOrder -- Unable to sell order " + order.getOrderID() + " holding already sold");
                order.cancel();
            } else {
                holdingRepository.delete(holding);
                order.setHolding(null);
                order.setOrderStatus("closed");
                order.setCompletionDate(new Timestamp(System.currentTimeMillis()));
                updateQuotePriceVolume(quote.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), quantity);
            }
        }

        orderRepository.save(order);
        return order;
    }

    @Transactional
    public void cancelOrder(Integer orderID, boolean twoPhase) {
        OrderDataBean order = orderRepository.findById(orderID).orElse(null);
        if (order != null) {
            order.cancel();
            orderRepository.save(order);
        }
    }

    @Transactional
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
        try {
            QuoteDataBean quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
            quoteRepository.save(quote);
            return quote;
        } catch (Exception e) {
            Log.error("TradeService:createQuote -- exception creating Quote", e);
            throw e;
        }
    }

    public QuoteDataBean getQuote(String symbol) throws Exception {
        return quoteRepository.findById(symbol).orElse(null);
    }

    public Collection<QuoteDataBean> getAllQuotes() throws Exception {
        return quoteRepository.findAll();
    }

    @Transactional
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) throws Exception {
        if (!TradeConfig.getUpdateQuotePrices()) {
            return new QuoteDataBean();
        }

        QuoteDataBean quote = quoteRepository.findBySymbolForUpdate(symbol);
        if (quote == null) {
            return new QuoteDataBean();
        }

        BigDecimal oldPrice = quote.getPrice();
        BigDecimal openPrice = quote.getOpen();

        if (oldPrice.equals(TradeConfig.PENNY_STOCK_PRICE)) {
            changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
        } else if (oldPrice.compareTo(TradeConfig.MAXIMUM_STOCK_PRICE) > 0) {
            changeFactor = TradeConfig.MAXIMUM_STOCK_SPLIT_MULTIPLIER;
        }

        BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, java.math.RoundingMode.HALF_UP);
        quote.setPrice(newPrice);
        quote.setChange(newPrice.subtract(openPrice).doubleValue());
        quote.setVolume(quote.getVolume() + sharesTraded);
        quoteRepository.save(quote);

        return quote;
    }

    public Collection<HoldingDataBean> getHoldings(String userID) throws Exception {
        return holdingRepository.findByAccountProfileUserID(userID);
    }

    public HoldingDataBean getHolding(Integer holdingID) throws Exception {
        return holdingRepository.findById(holdingID).orElse(null);
    }

    public AccountDataBean getAccountData(String userID) throws Exception {
        AccountProfileDataBean profile = accountProfileRepository.findById(userID)
            .orElseThrow(() -> new RuntimeException("No such user: " + userID));
        AccountDataBean account = profile.getAccount();
        if (account != null) {
            account.setProfileID(profile.getUserID());
        }
        return account;
    }

    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
        return accountProfileRepository.findById(userID).orElse(null);
    }

    @Transactional
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
        AccountProfileDataBean temp = accountProfileRepository.findById(profileData.getUserID())
            .orElseThrow(() -> new RuntimeException("No such user: " + profileData.getUserID()));
        temp.setAddress(profileData.getAddress());
        temp.setPassword(profileData.getPassword());
        temp.setFullName(profileData.getFullName());
        temp.setCreditCard(profileData.getCreditCard());
        temp.setEmail(profileData.getEmail());
        accountProfileRepository.save(temp);
        return temp;
    }

    @Transactional
    public AccountDataBean login(String userID, String password) throws Exception {
        AccountProfileDataBean profile = accountProfileRepository.findById(userID).orElse(null);
        if (profile == null) {
            throw new RuntimeException("No such user: " + userID);
        }
        AccountDataBean account = profile.getAccount();
        if (account == null) {
            throw new RuntimeException("No account for user: " + userID);
        }
        // login() validates password and increments loginCount
        account.login(password);
        accountRepository.save(account);
        return account;
    }

    @Transactional
    public void logout(String userID) throws Exception {
        AccountProfileDataBean profile = accountProfileRepository.findById(userID).orElse(null);
        if (profile != null) {
            AccountDataBean account = profile.getAccount();
            if (account != null) {
                account.logout();
                accountRepository.save(account);
            }
        }
    }

    @Transactional
    public AccountDataBean register(String userID, String password, String fullname,
            String address, String email, String creditcard, BigDecimal openBalance) throws Exception {
        // Check if user already exists
        if (accountProfileRepository.findById(userID).isPresent()) {
            Log.error("Failed to register new Account - AccountProfile with userID(" + userID + ") already exists");
            return null;
        }

        AccountProfileDataBean profile = new AccountProfileDataBean(userID, password, fullname, address, email, creditcard);
        // Save profile first (it has no FK to account) - use returned managed entity
        profile = accountProfileRepository.save(profile);

        AccountDataBean account = new AccountDataBean(0, 0, null,
            new Timestamp(System.currentTimeMillis()), openBalance, openBalance, userID);

        // Set the owning side FK reference (account -> profile)
        account.setProfile(profile);

        // Save account (owns the FK PROFILE_USERID)
        account = accountRepository.save(account);

        // Now set the inverse reference for in-memory consistency
        profile.setAccount(account);

        return account;
    }

    public Collection<OrderDataBean> getOrders(String userID) throws Exception {
        return orderRepository.findByAccountProfileUserID(userID);
    }

    @Transactional
    public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception {
        List<OrderDataBean> closedOrders = orderRepository.findClosedOrdersByUserID(userID);
        for (OrderDataBean order : closedOrders) {
            if (TradeConfig.getLongRun()) {
                orderRepository.delete(order);
            } else {
                order.setOrderStatus("completed");
                orderRepository.save(order);
            }
        }
        return closedOrders;
    }

    private OrderDataBean createOrder(AccountDataBean account, QuoteDataBean quote,
            HoldingDataBean holding, String orderType, double quantity) {
        OrderDataBean order = new OrderDataBean(orderType, "open",
            new Timestamp(System.currentTimeMillis()), null, quantity,
            quote.getPrice().setScale(FinancialUtils.SCALE, FinancialUtils.ROUND),
            TradeConfig.getOrderFee(orderType), account, quote, holding);
        orderRepository.save(order);
        return order;
    }
}
