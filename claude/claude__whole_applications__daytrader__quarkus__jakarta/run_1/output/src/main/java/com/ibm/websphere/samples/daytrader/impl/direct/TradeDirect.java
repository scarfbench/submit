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
package com.ibm.websphere.samples.daytrader.impl.direct;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import jakarta.transaction.UserTransaction;
import jakarta.validation.constraints.NotNull;

import com.ibm.websphere.samples.daytrader.messaging.MessageProducerService;

import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.MDBStats;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.interfaces.MarketSummaryUpdate;
import com.ibm.websphere.samples.daytrader.interfaces.RuntimeMode;
import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.interfaces.TradeJDBC;

@Dependent
@TradeJDBC
@RuntimeMode("Direct (JDBC)")
@Trace
public class TradeDirect implements TradeServices, Serializable {

  private static final long serialVersionUID = -8089049090952927985L;

  private static final Integer marketSummaryLock = new Integer(0);
  private static long nextMarketSummary = System.currentTimeMillis();
  private static MarketSummaryDataBean cachedMSDB = MarketSummaryDataBean.getRandomInstance();

  private static BigDecimal ZERO = new BigDecimal(0.0);
  private boolean inGlobalTxn = false;
  private boolean inSession = false;

  @Resource(lookup = "jdbc/TradeDataSource")
  private DataSource datasource;

  @Inject
  private UserTransaction txn;

  @Inject
  RecentQuotePriceChangeList recentQuotePriceChangeList;

  @Inject
  AsyncOrderSubmitter asyncOrderSubmitter;

  @Inject
  @MarketSummaryUpdate
  Event<String> mkSummaryUpdateEvent;

  @Inject
  MessageProducerService messageProducer;

  @Override
  public MarketSummaryDataBean getMarketSummary() throws Exception {
    if (TradeConfig.getMarketSummaryInterval() == 0) return getMarketSummaryInternal();
    if (TradeConfig.getMarketSummaryInterval() < 0) return cachedMSDB;
    long currentTime = System.currentTimeMillis();
    if (currentTime > nextMarketSummary) {
      long oldNextMarketSummary = nextMarketSummary;
      boolean fetch = false;
      synchronized (marketSummaryLock) {
        if (oldNextMarketSummary == nextMarketSummary) {
          fetch = true;
          nextMarketSummary += TradeConfig.getMarketSummaryInterval() * 1000;
          if (nextMarketSummary < currentTime) {
            nextMarketSummary = currentTime + TradeConfig.getMarketSummaryInterval() * 1000;
          }
        }
      }
      if (fetch) { cachedMSDB = getMarketSummaryInternal(); }
    }
    return cachedMSDB;
  }

  public MarketSummaryDataBean getMarketSummaryInternal() throws Exception {
    MarketSummaryDataBean marketSummaryData = null;
    Connection conn = null;
    try {
      conn = getConn();
      PreparedStatement stmt = getStatement(conn, getTSIAQuotesOrderByChangeSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ArrayList<QuoteDataBean> topGainersData = new ArrayList<>(5);
      ArrayList<QuoteDataBean> topLosersData = new ArrayList<>(5);
      ResultSet rs = stmt.executeQuery();
      int count = 0;
      while (rs.next() && (count++ < 5)) { topLosersData.add(getQuoteDataFromResultSet(rs)); }
      stmt.close();
      stmt = getStatement(conn, "select * from quoteejb q order by q.change1 DESC", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      rs = stmt.executeQuery();
      count = 0;
      while (rs.next() && (count++ < 5)) { topGainersData.add(getQuoteDataFromResultSet(rs)); }
      stmt.close();
      BigDecimal TSIA = ZERO; BigDecimal openTSIA = ZERO; double volume = 0.0;
      if ((topGainersData.size() > 0) || (topLosersData.size() > 0)) {
        stmt = getStatement(conn, getTSIASQL); rs = stmt.executeQuery();
        if (!rs.next()) { Log.error("TradeDirect:getMarketSummary -- error w/ getTSIASQL"); } else { TSIA = rs.getBigDecimal("TSIA"); }
        stmt.close();
        stmt = getStatement(conn, getOpenTSIASQL); rs = stmt.executeQuery();
        if (!rs.next()) { Log.error("TradeDirect:getMarketSummary -- error w/ getOpenTSIASQL"); } else { openTSIA = rs.getBigDecimal("openTSIA"); }
        stmt.close();
        stmt = getStatement(conn, getTSIATotalVolumeSQL); rs = stmt.executeQuery();
        if (!rs.next()) { Log.error("TradeDirect:getMarketSummary -- error w/ getTSIATotalVolumeSQL"); } else { volume = rs.getDouble("totalVolume"); }
        stmt.close();
      }
      commit(conn);
      marketSummaryData = new MarketSummaryDataBean(TSIA, openTSIA, volume, topGainersData, topLosersData);
      mkSummaryUpdateEvent.fireAsync("MarketSummaryUpdate");
    } catch (Exception e) {
      Log.error("TradeDirect:login -- error logging in user", e); rollBack(conn, e);
    } finally { releaseConn(conn); }
    return marketSummaryData;
  }

  @Override
  @NotNull
  public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
    final Connection conn = getConn(); OrderDataBean orderData = null; BigDecimal total;
    try {
      if (!inSession && orderProcessingMode == TradeConfig.ASYNCH_2PHASE) { txn.begin(); setInGlobalTxn(true); }
      AccountDataBean accountData = getAccountData(conn, userID);
      QuoteDataBean quoteData = getQuoteData(conn, symbol);
      HoldingDataBean holdingData = null;
      orderData = createOrder(accountData, quoteData, holdingData, "buy", quantity);
      BigDecimal price = quoteData.getPrice(); BigDecimal orderFee = orderData.getOrderFee();
      total = (new BigDecimal(quantity).multiply(price)).add(orderFee);
      creditAccountBalance(conn, accountData, total.negate());
      final Integer orderID = orderData.getOrderID();
      try {
        if (orderProcessingMode == TradeConfig.SYNCH) { completeOrder(conn, orderData.getOrderID()); }
        else if (orderProcessingMode == TradeConfig.ASYNCH) { completeOrderAsync(orderID, true); }
        else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE) { queueOrder(orderID, true); }
      } catch (Exception je) { Log.error("TradeBean:buy failed to queueOrder", je); cancelOrder(conn, orderData.getOrderID()); }
      orderData = getOrderData(conn, orderData.getOrderID().intValue());
      if (getInGlobalTxn()) {
        if (!inSession && orderProcessingMode == TradeConfig.ASYNCH_2PHASE) { txn.commit(); setInGlobalTxn(false); }
      } else { commit(conn); }
    } catch (Exception e) {
      Log.error("TradeDirect:buy error", e);
      if (getInGlobalTxn()) { txn.rollback(); } else { rollBack(conn, e); }
    } finally { releaseConn(conn); }
    return orderData;
  }

  @Override
  @NotNull
  public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
    Connection conn = null; OrderDataBean orderData = null; BigDecimal total;
    try {
      if (!inSession && orderProcessingMode == TradeConfig.ASYNCH_2PHASE) { txn.begin(); setInGlobalTxn(true); }
      conn = getConn();
      AccountDataBean accountData = getAccountData(conn, userID);
      HoldingDataBean holdingData = getHoldingData(conn, holdingID.intValue());
      QuoteDataBean quoteData = null;
      if (holdingData != null) { quoteData = getQuoteData(conn, holdingData.getQuoteID()); }
      if ((accountData == null) || (holdingData == null) || (quoteData == null)) {
        if (getInGlobalTxn()) { txn.rollback(); } else { rollBack(conn, new Exception("missing data")); }
        orderData = new OrderDataBean(); orderData.setOrderStatus("cancelled"); return orderData;
      }
      double quantity = holdingData.getQuantity();
      orderData = createOrder(accountData, quoteData, holdingData, "sell", quantity);
      updateHoldingStatus(conn, holdingData.getHoldingID(), holdingData.getQuoteID());
      BigDecimal price = quoteData.getPrice(); BigDecimal orderFee = orderData.getOrderFee();
      total = (new BigDecimal(quantity).multiply(price)).subtract(orderFee);
      creditAccountBalance(conn, accountData, total);
      try {
        if (orderProcessingMode == TradeConfig.SYNCH) { completeOrder(conn, orderData.getOrderID()); }
        else if (orderProcessingMode == TradeConfig.ASYNCH) { this.completeOrderAsync(orderData.getOrderID(), true); }
        else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE) { queueOrder(orderData.getOrderID(), true); }
      } catch (Exception je) { cancelOrder(conn, orderData.getOrderID()); }
      orderData = getOrderData(conn, orderData.getOrderID().intValue());
      if (!inSession && orderProcessingMode == TradeConfig.ASYNCH_2PHASE) { txn.commit(); setInGlobalTxn(false); }
      else { commit(conn); }
    } catch (Exception e) {
      Log.error("TradeDirect:sell error", e);
      if (getInGlobalTxn()) { txn.rollback(); } else { rollBack(conn, e); }
    } finally { releaseConn(conn); }
    return orderData;
  }

  @Override
  public void queueOrder(Integer orderID, boolean twoPhase) throws Exception {
    messageProducer.queueOrderForProcessing(orderID, twoPhase);
  }

  @Override
  public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception {
    OrderDataBean orderData = null; Connection conn = null;
    try {
      setInGlobalTxn(!inSession && twoPhase); conn = getConn();
      orderData = completeOrder(conn, orderID); commit(conn);
    } catch (Exception e) {
      Log.error("TradeDirect:completeOrder error", e); rollBack(conn, e); cancelOrder(orderID, twoPhase);
    } finally { releaseConn(conn); }
    return orderData;
  }

  @Override
  public Future<OrderDataBean> completeOrderAsync(Integer orderID, boolean twoPhase) throws Exception {
    if (!inSession) { asyncOrderSubmitter.submitOrder(orderID, twoPhase); }
    return null;
  }

  private OrderDataBean completeOrder(Connection conn, Integer orderID) throws Exception {
    OrderDataBean orderData = null;
    PreparedStatement stmt = getStatement(conn, getOrderSQL); stmt.setInt(1, orderID.intValue());
    ResultSet rs = stmt.executeQuery();
    if (!rs.next()) { stmt.close(); return orderData; }
    orderData = getOrderDataFromResultSet(rs);
    String orderType = orderData.getOrderType(); String orderStatus = orderData.getOrderStatus();
    if ((orderStatus.compareToIgnoreCase("completed") == 0) || (orderStatus.compareToIgnoreCase("alertcompleted") == 0) || (orderStatus.compareToIgnoreCase("cancelled") == 0))
      throw new Exception("attempt to complete already completed Order");
    int accountID = rs.getInt("account_accountID"); String quoteID = rs.getString("quote_symbol"); int holdingID = rs.getInt("holding_holdingID");
    BigDecimal price = orderData.getPrice(); double quantity = orderData.getQuantity();
    String userID = getAccountProfileData(conn, new Integer(accountID)).getUserID();
    HoldingDataBean holdingData = null;
    if (orderType.compareToIgnoreCase("buy") == 0) {
      holdingData = createHolding(conn, accountID, quoteID, quantity, price);
      updateOrderHolding(conn, orderID.intValue(), holdingData.getHoldingID().intValue());
      updateOrderStatus(conn, orderData.getOrderID(), "closed");
      updateQuotePriceVolume(orderData.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), orderData.getQuantity());
    }
    if (orderType.compareToIgnoreCase("sell") == 0) {
      holdingData = getHoldingData(conn, holdingID);
      if (holdingData == null) { updateOrderStatus(conn, orderData.getOrderID(), "cancelled"); }
      else { removeHolding(conn, holdingID, orderID.intValue()); updateOrderStatus(conn, orderData.getOrderID(), "closed");
        updateQuotePriceVolume(orderData.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), orderData.getQuantity()); }
    }
    stmt.close(); commit(conn);
    return orderData;
  }

  @Override
  public void cancelOrder(Integer orderID, boolean twoPhase) throws Exception {
    Connection conn = null;
    try { setInGlobalTxn(!inSession && twoPhase); conn = getConn(); cancelOrder(conn, orderID); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
  }
  private void cancelOrder(Connection conn, Integer orderID) throws Exception { updateOrderStatus(conn, orderID, "cancelled"); }

  @Override
  public void orderCompleted(String userID, Integer orderID) throws Exception { throw new UnsupportedOperationException(); }

  private HoldingDataBean createHolding(Connection conn, int accountID, String symbol, double quantity, BigDecimal purchasePrice) throws Exception {
    Timestamp purchaseDate = new Timestamp(System.currentTimeMillis());
    PreparedStatement stmt = getStatement(conn, createHoldingSQL);
    Integer holdingID = KeySequenceDirect.getNextID(conn, "holding", inSession, getInGlobalTxn());
    stmt.setInt(1, holdingID); stmt.setTimestamp(2, purchaseDate); stmt.setBigDecimal(3, purchasePrice);
    stmt.setDouble(4, quantity); stmt.setString(5, symbol); stmt.setInt(6, accountID); stmt.executeUpdate(); stmt.close();
    return getHoldingData(conn, holdingID);
  }
  private void removeHolding(Connection conn, int holdingID, int orderID) throws Exception {
    PreparedStatement stmt = getStatement(conn, removeHoldingSQL); stmt.setInt(1, holdingID); stmt.executeUpdate(); stmt.close();
    stmt = getStatement(conn, removeHoldingFromOrderSQL); stmt.setInt(1, holdingID); stmt.executeUpdate(); stmt.close();
  }
  public OrderDataBean createOrder(AccountDataBean accountData, QuoteDataBean quoteData, HoldingDataBean holdingData, String orderType, double quantity) throws Exception {
    OrderDataBean orderData = null; Connection conn = null;
    try {
      conn = getConn(); Timestamp currentDate = new Timestamp(System.currentTimeMillis());
      PreparedStatement stmt = getStatement(conn, createOrderSQL);
      Integer orderID = KeySequenceDirect.getNextID(conn, "order", inSession, getInGlobalTxn());
      stmt.setInt(1, orderID); stmt.setString(2, orderType); stmt.setString(3, "open"); stmt.setTimestamp(4, currentDate);
      stmt.setDouble(5, quantity); stmt.setBigDecimal(6, quoteData.getPrice().setScale(FinancialUtils.SCALE, FinancialUtils.ROUND));
      stmt.setBigDecimal(7, TradeConfig.getOrderFee(orderType)); stmt.setInt(8, accountData.getAccountID());
      if (holdingData == null) { stmt.setNull(9, java.sql.Types.INTEGER); } else { stmt.setInt(9, holdingData.getHoldingID()); }
      stmt.setString(10, quoteData.getSymbol()); stmt.executeUpdate();
      orderData = getOrderData(conn, orderID); stmt.close(); commit(conn);
    } catch (Exception e) { Log.error("TradeDirect:createOrder error", e); rollBack(conn, e); } finally { releaseConn(conn); }
    return orderData;
  }
  @Override
  public Collection<OrderDataBean> getOrders(String userID) throws Exception {
    Collection<OrderDataBean> orderDataBeans = new ArrayList<>(); Connection conn = null;
    try { conn = getConn(); PreparedStatement stmt = getStatement(conn, getOrdersByUserSQL); stmt.setString(1, userID);
      ResultSet rs = stmt.executeQuery(); int i = 0;
      while ((rs.next()) && (i++ < 5)) { orderDataBeans.add(getOrderDataFromResultSet(rs)); }
      stmt.close(); commit(conn);
    } catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return orderDataBeans;
  }
  @Override
  public Collection<OrderDataBean> getClosedOrders(String userID) throws Exception {
    Collection<OrderDataBean> orderDataBeans = new ArrayList<>(); Connection conn = null;
    try { conn = getConn(); PreparedStatement stmt = getStatement(conn, getClosedOrdersSQL); stmt.setString(1, userID);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) { OrderDataBean orderData = getOrderDataFromResultSet(rs); orderData.setOrderStatus("completed");
        updateOrderStatus(conn, orderData.getOrderID(), orderData.getOrderStatus()); orderDataBeans.add(orderData); }
      stmt.close(); commit(conn);
    } catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return orderDataBeans;
  }
  @Override
  public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
    QuoteDataBean quoteData = null; Connection conn = null;
    try { price = price.setScale(FinancialUtils.SCALE, FinancialUtils.ROUND);
      conn = getConn(); PreparedStatement stmt = getStatement(conn, createQuoteSQL);
      stmt.setString(1, symbol); stmt.setString(2, companyName); stmt.setDouble(3, 0.0);
      stmt.setBigDecimal(4, price); stmt.setBigDecimal(5, price); stmt.setBigDecimal(6, price);
      stmt.setBigDecimal(7, price); stmt.setDouble(8, 0.0); stmt.executeUpdate(); stmt.close(); commit(conn);
      quoteData = new QuoteDataBean(symbol, companyName, 0.0, price, price, price, price, 0.0);
    } catch (Exception e) { Log.error("TradeDirect:createQuote error", e); } finally { releaseConn(conn); }
    return quoteData;
  }
  @Override
  public QuoteDataBean getQuote(String symbol) throws Exception {
    QuoteDataBean quoteData = null; Connection conn = null;
    try { conn = getConn(); quoteData = getQuote(conn, symbol); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return quoteData;
  }
  private QuoteDataBean getQuote(Connection conn, String symbol) throws Exception {
    QuoteDataBean quoteData = null; PreparedStatement stmt = getStatement(conn, getQuoteSQL); stmt.setString(1, symbol);
    ResultSet rs = stmt.executeQuery(); if (!rs.next()) { Log.error("TradeDirect:getQuote -- no result for: " + symbol); } else { quoteData = getQuoteDataFromResultSet(rs); }
    stmt.close(); return quoteData;
  }
  private QuoteDataBean getQuoteForUpdate(Connection conn, String symbol) throws Exception {
    QuoteDataBean quoteData = null; PreparedStatement stmt = getStatement(conn, getQuoteForUpdateSQL); stmt.setString(1, symbol);
    ResultSet rs = stmt.executeQuery(); if (!rs.next()) { Log.error("TradeDirect:getQuote -- no result"); } else { quoteData = getQuoteDataFromResultSet(rs); }
    stmt.close(); return quoteData;
  }
  @Override
  public Collection<QuoteDataBean> getAllQuotes() throws Exception {
    Collection<QuoteDataBean> quotes = new ArrayList<>(); Connection conn = null;
    try { conn = getConn(); PreparedStatement stmt = getStatement(conn, getAllQuotesSQL); ResultSet rs = stmt.executeQuery();
      while (rs.next()) { quotes.add(getQuoteDataFromResultSet(rs)); } stmt.close();
    } catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return quotes;
  }
  @Override
  public Collection<HoldingDataBean> getHoldings(String userID) throws Exception {
    Collection<HoldingDataBean> holdingDataBeans = new ArrayList<>(); Connection conn = null;
    try { conn = getConn(); PreparedStatement stmt = getStatement(conn, getHoldingsForUserSQL); stmt.setString(1, userID);
      ResultSet rs = stmt.executeQuery(); while (rs.next()) { holdingDataBeans.add(getHoldingDataFromResultSet(rs)); }
      stmt.close(); commit(conn);
    } catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return holdingDataBeans;
  }
  @Override
  public HoldingDataBean getHolding(Integer holdingID) throws Exception {
    HoldingDataBean holdingData = null; Connection conn = null;
    try { conn = getConn(); holdingData = getHoldingData(holdingID.intValue()); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return holdingData;
  }
  @Override
  public AccountDataBean getAccountData(String userID) throws Exception {
    AccountDataBean accountData = null; Connection conn = null;
    try { conn = getConn(); accountData = getAccountData(conn, userID); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return accountData;
  }
  private AccountDataBean getAccountData(Connection conn, String userID) throws Exception {
    PreparedStatement stmt = getStatement(conn, getAccountForUserSQL); stmt.setString(1, userID);
    ResultSet rs = stmt.executeQuery(); AccountDataBean accountData = getAccountDataFromResultSet(rs); stmt.close(); return accountData;
  }
  public AccountDataBean getAccountData(int accountID) throws Exception {
    AccountDataBean accountData = null; Connection conn = null;
    try { conn = getConn(); accountData = getAccountData(accountID, conn); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return accountData;
  }
  private AccountDataBean getAccountData(int accountID, Connection conn) throws Exception {
    PreparedStatement stmt = getStatement(conn, getAccountSQL); stmt.setInt(1, accountID);
    ResultSet rs = stmt.executeQuery(); AccountDataBean accountData = getAccountDataFromResultSet(rs); stmt.close(); return accountData;
  }
  private QuoteDataBean getQuoteData(Connection conn, String symbol) throws Exception {
    QuoteDataBean quoteData = null; PreparedStatement stmt = getStatement(conn, getQuoteSQL); stmt.setString(1, symbol);
    ResultSet rs = stmt.executeQuery(); if (rs.next()) { quoteData = getQuoteDataFromResultSet(rs); } stmt.close(); return quoteData;
  }
  private HoldingDataBean getHoldingData(int holdingID) throws Exception {
    HoldingDataBean holdingData = null; Connection conn = null;
    try { conn = getConn(); holdingData = getHoldingData(conn, holdingID); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return holdingData;
  }
  private HoldingDataBean getHoldingData(Connection conn, int holdingID) throws Exception {
    HoldingDataBean holdingData = null; PreparedStatement stmt = getStatement(conn, getHoldingSQL); stmt.setInt(1, holdingID);
    ResultSet rs = stmt.executeQuery(); if (rs.next()) { holdingData = getHoldingDataFromResultSet(rs); } stmt.close(); return holdingData;
  }
  private OrderDataBean getOrderData(Connection conn, int orderID) throws Exception {
    OrderDataBean orderData = null; PreparedStatement stmt = getStatement(conn, getOrderSQL); stmt.setInt(1, orderID);
    ResultSet rs = stmt.executeQuery(); if (rs.next()) { orderData = getOrderDataFromResultSet(rs); } stmt.close(); return orderData;
  }
  @Override
  public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
    AccountProfileDataBean accountProfileData = null; Connection conn = null;
    try { conn = getConn(); accountProfileData = getAccountProfileData(conn, userID); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return accountProfileData;
  }
  private AccountProfileDataBean getAccountProfileData(Connection conn, String userID) throws Exception {
    PreparedStatement stmt = getStatement(conn, getAccountProfileSQL); stmt.setString(1, userID);
    ResultSet rs = stmt.executeQuery(); AccountProfileDataBean d = getAccountProfileDataFromResultSet(rs); stmt.close(); return d;
  }
  private AccountProfileDataBean getAccountProfileData(Connection conn, Integer accountID) throws Exception {
    PreparedStatement stmt = getStatement(conn, getAccountProfileForAccountSQL); stmt.setInt(1, accountID);
    ResultSet rs = stmt.executeQuery(); AccountProfileDataBean d = getAccountProfileDataFromResultSet(rs); stmt.close(); return d;
  }
  @Override
  public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
    AccountProfileDataBean accountProfileData = null; Connection conn = null;
    try { conn = getConn(); updateAccountProfile(conn, profileData); accountProfileData = getAccountProfileData(conn, profileData.getUserID()); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
    return accountProfileData;
  }
  private void creditAccountBalance(Connection conn, AccountDataBean accountData, BigDecimal credit) throws Exception {
    PreparedStatement stmt = getStatement(conn, creditAccountBalanceSQL); stmt.setBigDecimal(1, credit); stmt.setInt(2, accountData.getAccountID()); stmt.executeUpdate(); stmt.close();
  }
  private void updateHoldingStatus(Connection conn, Integer holdingID, String symbol) throws Exception {
    PreparedStatement stmt = getStatement(conn, "update holdingejb set purchasedate= ? where holdingid = ?");
    stmt.setTimestamp(1, new Timestamp(0)); stmt.setInt(2, holdingID); stmt.executeUpdate(); stmt.close();
  }
  private void updateOrderStatus(Connection conn, Integer orderID, String status) throws Exception {
    PreparedStatement stmt = getStatement(conn, updateOrderStatusSQL); stmt.setString(1, status);
    stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis())); stmt.setInt(3, orderID); stmt.executeUpdate(); stmt.close();
  }
  private void updateOrderHolding(Connection conn, int orderID, int holdingID) throws Exception {
    PreparedStatement stmt = getStatement(conn, updateOrderHoldingSQL); stmt.setInt(1, holdingID); stmt.setInt(2, orderID); stmt.executeUpdate(); stmt.close();
  }
  private void updateAccountProfile(Connection conn, AccountProfileDataBean profileData) throws Exception {
    PreparedStatement stmt = getStatement(conn, updateAccountProfileSQL);
    stmt.setString(1, profileData.getPassword()); stmt.setString(2, profileData.getFullName());
    stmt.setString(3, profileData.getAddress()); stmt.setString(4, profileData.getEmail());
    stmt.setString(5, profileData.getCreditCard()); stmt.setString(6, profileData.getUserID()); stmt.executeUpdate(); stmt.close();
  }
  @Override
  public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) throws Exception {
    return updateQuotePriceVolumeInt(symbol, changeFactor, sharesTraded, TradeConfig.getPublishQuotePriceChange());
  }
  public QuoteDataBean updateQuotePriceVolumeInt(String symbol, BigDecimal changeFactor, double sharesTraded, boolean publishQuotePriceChange) throws Exception {
    if (!TradeConfig.getUpdateQuotePrices()) return new QuoteDataBean();
    QuoteDataBean quoteData = null; Connection conn = null;
    try {
      conn = getConn(); quoteData = getQuoteForUpdate(conn, symbol);
      BigDecimal oldPrice = quoteData.getPrice(); BigDecimal openPrice = quoteData.getOpen();
      double newVolume = quoteData.getVolume() + sharesTraded;
      if (oldPrice.equals(TradeConfig.PENNY_STOCK_PRICE)) { changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER; }
      else if (oldPrice.compareTo(TradeConfig.MAXIMUM_STOCK_PRICE) > 0) { changeFactor = TradeConfig.MAXIMUM_STOCK_SPLIT_MULTIPLIER; }
      BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
      double change = newPrice.subtract(openPrice).doubleValue();
      updateQuotePriceVolume(conn, quoteData.getSymbol(), newPrice, newVolume, change);
      quoteData = getQuote(conn, symbol); commit(conn);
      if (publishQuotePriceChange) { publishQuotePriceChange(quoteData, oldPrice, changeFactor, sharesTraded); }
      recentQuotePriceChangeList.add(quoteData);
    } catch (Exception e) { rollBack(conn, e); throw e; } finally { releaseConn(conn); }
    return quoteData;
  }
  private void updateQuotePriceVolume(Connection conn, String symbol, BigDecimal newPrice, double newVolume, double change) throws Exception {
    PreparedStatement stmt = getStatement(conn, updateQuotePriceVolumeSQL);
    stmt.setBigDecimal(1, newPrice); stmt.setDouble(2, change); stmt.setDouble(3, newVolume); stmt.setString(4, symbol); stmt.executeUpdate(); stmt.close();
  }
  private void publishQuotePriceChange(QuoteDataBean quoteData, BigDecimal oldPrice, BigDecimal changeFactor, double sharesTraded) throws Exception {
    messageProducer.publishQuotePriceChange(quoteData.getSymbol(), quoteData.getCompanyName(), quoteData.getPrice(),
        oldPrice, quoteData.getOpen(), quoteData.getLow(), quoteData.getHigh(), quoteData.getVolume(), changeFactor, sharesTraded);
  }
  @Override
  public AccountDataBean login(String userID, String password) throws Exception {
    AccountDataBean accountData = null; Connection conn = null;
    try {
      conn = getConn(); PreparedStatement stmt = getStatement(conn, getAccountProfileSQL); stmt.setString(1, userID);
      ResultSet rs = stmt.executeQuery();
      if (!rs.next()) throw new RuntimeException("Cannot find account for " + userID);
      String pw = rs.getString("passwd"); stmt.close();
      if ((pw == null) || (!pw.equals(password))) throw new Exception("Login failure for user: " + userID);
      stmt = getStatement(conn, loginSQL); stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis())); stmt.setString(2, userID); stmt.executeUpdate(); stmt.close();
      stmt = getStatement(conn, getAccountForUserSQL); stmt.setString(1, userID); rs = stmt.executeQuery();
      accountData = getAccountDataFromResultSet(rs); stmt.close(); commit(conn);
    } catch (Exception e) { Log.error("TradeDirect:login error", e); rollBack(conn, e); } finally { releaseConn(conn); }
    return accountData;
  }
  @Override
  public void logout(String userID) throws Exception {
    Connection conn = null;
    try { conn = getConn(); PreparedStatement stmt = getStatement(conn, logoutSQL); stmt.setString(1, userID); stmt.executeUpdate(); stmt.close(); commit(conn); }
    catch (Exception e) { rollBack(conn, e); } finally { releaseConn(conn); }
  }
  @Override
  public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditcard, BigDecimal openBalance) throws Exception {
    AccountDataBean accountData = null; Connection conn = null;
    try {
      conn = getConn(); PreparedStatement stmt = getStatement(conn, createAccountSQL);
      Integer accountID = KeySequenceDirect.getNextID(conn, "account", inSession, getInGlobalTxn());
      Timestamp creationDate = new Timestamp(System.currentTimeMillis());
      stmt.setInt(1, accountID); stmt.setTimestamp(2, creationDate); stmt.setBigDecimal(3, openBalance); stmt.setBigDecimal(4, openBalance);
      stmt.setTimestamp(5, creationDate); stmt.setInt(6, 0); stmt.setInt(7, 0); stmt.setString(8, userID); stmt.executeUpdate(); stmt.close();
      stmt = getStatement(conn, createAccountProfileSQL);
      stmt.setString(1, userID); stmt.setString(2, password); stmt.setString(3, fullname);
      stmt.setString(4, address); stmt.setString(5, email); stmt.setString(6, creditcard); stmt.executeUpdate(); stmt.close();
      commit(conn);
      accountData = new AccountDataBean(accountID, 0, 0, creationDate, creationDate, openBalance, openBalance, userID);
    } catch (Exception e) { Log.error("TradeDirect:register error", e); } finally { releaseConn(conn); }
    return accountData;
  }

  private AccountDataBean getAccountDataFromResultSet(ResultSet rs) throws Exception {
    AccountDataBean accountData = null;
    if (!rs.next()) { Log.error("TradeDirect:getAccountDataFromResultSet -- cannot find account data"); }
    else { accountData = new AccountDataBean(rs.getInt("accountID"), rs.getInt("loginCount"), rs.getInt("logoutCount"),
      rs.getTimestamp("lastLogin"), rs.getTimestamp("creationDate"), rs.getBigDecimal("balance"), rs.getBigDecimal("openBalance"), rs.getString("profile_userID")); }
    return accountData;
  }
  private AccountProfileDataBean getAccountProfileDataFromResultSet(ResultSet rs) throws Exception {
    AccountProfileDataBean d = null;
    if (!rs.next()) { Log.error("TradeDirect:getAccountProfileDataFromResultSet -- cannot find data"); }
    else { d = new AccountProfileDataBean(rs.getString("userID"), rs.getString("passwd"), rs.getString("fullName"), rs.getString("address"), rs.getString("email"), rs.getString("creditCard")); }
    return d;
  }
  private HoldingDataBean getHoldingDataFromResultSet(ResultSet rs) throws Exception {
    return new HoldingDataBean(rs.getInt("holdingID"), rs.getDouble("quantity"), rs.getBigDecimal("purchasePrice"), rs.getTimestamp("purchaseDate"), rs.getString("quote_symbol"));
  }
  private QuoteDataBean getQuoteDataFromResultSet(ResultSet rs) throws Exception {
    return new QuoteDataBean(rs.getString("symbol"), rs.getString("companyName"), rs.getDouble("volume"), rs.getBigDecimal("price"),
      rs.getBigDecimal("open1"), rs.getBigDecimal("low"), rs.getBigDecimal("high"), rs.getDouble("change1"));
  }
  private OrderDataBean getOrderDataFromResultSet(ResultSet rs) throws Exception {
    return new OrderDataBean(rs.getInt("orderID"), rs.getString("orderType"), rs.getString("orderStatus"), rs.getTimestamp("openDate"),
      rs.getTimestamp("completionDate"), rs.getDouble("quantity"), rs.getBigDecimal("price"), rs.getBigDecimal("orderFee"), rs.getString("quote_symbol"));
  }

  private void releaseConn(Connection conn) throws Exception { try { if (conn != null) conn.close(); } catch (Exception e) { Log.error("TradeDirect:releaseConnection error", e); } }
  private static int connCount = 0;
  private static Integer lock = new Integer(0);
  private Connection getConn() throws Exception {
    Connection conn = datasource.getConnection();
    if (!this.inGlobalTxn) conn.setAutoCommit(false);
    return conn;
  }
  public Connection getConnPublic() throws Exception { return getConn(); }
  private void commit(Connection conn) throws Exception { if (!inSession) { if (!getInGlobalTxn() && conn != null) conn.commit(); } }
  private void rollBack(Connection conn, Exception e) throws Exception {
    if (!inSession) { if (!getInGlobalTxn() && conn != null) conn.rollback(); else throw e; }
  }
  private PreparedStatement getStatement(Connection conn, String sql) throws Exception { return conn.prepareStatement(sql); }
  private PreparedStatement getStatement(Connection conn, String sql, int type, int concurrency) throws Exception { return conn.prepareStatement(sql, type, concurrency); }

  private static final String createQuoteSQL = "insert into quoteejb (symbol, companyName, volume, price, open1, low, high, change1) VALUES (?,?,?,?,?,?,?,?)";
  private static final String createAccountSQL = "insert into accountejb (accountid, creationDate, openBalance, balance, lastLogin, loginCount, logoutCount, profile_userid) VALUES (?,?,?,?,?,?,?,?)";
  private static final String createAccountProfileSQL = "insert into accountprofileejb (userid, passwd, fullname, address, email, creditcard) VALUES (?,?,?,?,?,?)";
  private static final String createHoldingSQL = "insert into holdingejb (holdingid, purchaseDate, purchasePrice, quantity, quote_symbol, account_accountid) VALUES (?,?,?,?,?,?)";
  private static final String createOrderSQL = "insert into orderejb (orderid, ordertype, orderstatus, opendate, quantity, price, orderfee, account_accountid, holding_holdingid, quote_symbol) VALUES (?,?,?,?,?,?,?,?,?,?)";
  private static final String removeHoldingSQL = "delete from holdingejb where holdingid = ?";
  private static final String removeHoldingFromOrderSQL = "update orderejb set holding_holdingid=null where holding_holdingid = ?";
  private static final String updateAccountProfileSQL = "update accountprofileejb set passwd = ?, fullname = ?, address = ?, email = ?, creditcard = ? where userid = (select profile_userid from accountejb a where a.profile_userid=?)";
  private static final String loginSQL = "update accountejb set lastLogin=?, logincount=logincount+1 where profile_userid=?";
  private static final String logoutSQL = "update accountejb set logoutcount=logoutcount+1 where profile_userid=?";
  private static final String getAccountSQL = "select * from accountejb a where a.accountid = ?";
  private static final String getAccountProfileSQL = "select * from accountprofileejb ap where ap.userid = (select profile_userid from accountejb a where a.profile_userid=?)";
  private static final String getAccountProfileForAccountSQL = "select * from accountprofileejb ap where ap.userid = (select profile_userid from accountejb a where a.accountid=?)";
  private static final String getAccountForUserSQL = "select * from accountejb a where a.profile_userid = (select userid from accountprofileejb ap where ap.userid = ?)";
  private static final String getHoldingSQL = "select * from holdingejb h where h.holdingid = ?";
  private static final String getHoldingsForUserSQL = "select * from holdingejb h where h.account_accountid = (select a.accountid from accountejb a where a.profile_userid = ?)";
  private static final String getOrderSQL = "select * from orderejb o where o.orderid = ?";
  private static final String getOrdersByUserSQL = "select * from orderejb o where o.account_accountid = (select a.accountid from accountejb a where a.profile_userid = ?)";
  private static final String getClosedOrdersSQL = "select * from orderejb o where o.orderstatus = 'closed' AND o.account_accountid = (select a.accountid from accountejb a where a.profile_userid = ?)";
  private static final String getQuoteSQL = "select * from quoteejb q where q.symbol=?";
  private static final String getAllQuotesSQL = "select * from quoteejb q";
  private static final String getQuoteForUpdateSQL = "select * from quoteejb q where q.symbol=? For Update";
  private static final String getTSIAQuotesOrderByChangeSQL = "select * from quoteejb q order by q.change1";
  private static final String getTSIASQL = "select SUM(price)/count(*) as TSIA from quoteejb q";
  private static final String getOpenTSIASQL = "select SUM(open1)/count(*) as openTSIA from quoteejb q";
  private static final String getTSIATotalVolumeSQL = "select SUM(volume) as totalVolume from quoteejb q";
  private static final String creditAccountBalanceSQL = "update accountejb set balance = balance + ? where accountid = ?";
  private static final String updateOrderStatusSQL = "update orderejb set orderstatus = ?, completiondate = ? where orderid = ?";
  private static final String updateOrderHoldingSQL = "update orderejb set holding_holdingID = ? where orderid = ?";
  private static final String updateQuotePriceVolumeSQL = "update quoteejb set price = ?, change1 = ?, volume = ? where symbol = ?";

  private boolean getInGlobalTxn() { return inGlobalTxn; }
  private void setInGlobalTxn(boolean inGlobalTxn) { this.inGlobalTxn = inGlobalTxn; }
  public void setInSession(boolean inSession) { this.inSession = inSession; }
  @Override public int getImpl() { return TradeConfig.DIRECT; }
  @Override public QuoteDataBean pingTwoPhase(String symbol) { throw new UnsupportedOperationException(); }
  @Override public double investmentReturn(double rnd1, double rnd2) { throw new UnsupportedOperationException(); }
}
