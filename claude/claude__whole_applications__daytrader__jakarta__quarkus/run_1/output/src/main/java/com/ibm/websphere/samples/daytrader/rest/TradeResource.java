/**
 * JAX-RS REST resource replacing JSF TradeAppJSF and other JSF managed beans.
 * Provides REST endpoints for all trading operations.
 */
package com.ibm.websphere.samples.daytrader.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.service.TradeServiceBean;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TradeResource {

    @Inject
    TradeServiceBean tradeService;

    // ==================== Market Summary ====================

    @GET
    @Path("/marketSummary")
    public Response getMarketSummary() {
        try {
            MarketSummaryDataBean summary = tradeService.getMarketSummary();
            return Response.ok(summary).build();
        } catch (Exception e) {
            Log.error("TradeResource:getMarketSummary", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    // ==================== Account Operations ====================

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response login(@FormParam("userID") String userID, @FormParam("password") String password) {
        try {
            AccountDataBean account = tradeService.login(userID, password);
            return Response.ok(account).build();
        } catch (Exception e) {
            Log.error("TradeResource:login", e);
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Login failed: " + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/logout/{userID}")
    @Transactional
    public Response logout(@PathParam("userID") String userID) {
        try {
            tradeService.logout(userID);
            return Response.ok("{\"status\":\"logged out\"}").build();
        } catch (Exception e) {
            Log.error("TradeResource:logout", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response register(@FormParam("userID") String userID, @FormParam("password") String password,
                             @FormParam("fullname") String fullname, @FormParam("address") String address,
                             @FormParam("email") String email, @FormParam("creditcard") String creditcard,
                             @FormParam("openBalance") String openBalance) {
        try {
            BigDecimal balance = new BigDecimal(openBalance != null ? openBalance : "100000");
            AccountDataBean account = tradeService.register(userID, password, fullname, address, email, creditcard, balance);
            if (account == null) {
                return Response.status(Response.Status.CONFLICT).entity("{\"error\":\"User already exists\"}").build();
            }
            return Response.ok(account).build();
        } catch (Exception e) {
            Log.error("TradeResource:register", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/account/{userID}")
    public Response getAccountData(@PathParam("userID") String userID) {
        try {
            AccountDataBean account = tradeService.getAccountData(userID);
            if (account == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"User not found\"}").build();
            }
            return Response.ok(account).build();
        } catch (Exception e) {
            Log.error("TradeResource:getAccountData", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/account/{userID}/profile")
    public Response getAccountProfile(@PathParam("userID") String userID) {
        try {
            AccountProfileDataBean profile = tradeService.getAccountProfileData(userID);
            if (profile == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Profile not found\"}").build();
            }
            return Response.ok(profile).build();
        } catch (Exception e) {
            Log.error("TradeResource:getAccountProfile", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @PUT
    @Path("/account/{userID}/profile")
    @Transactional
    public Response updateAccountProfile(@PathParam("userID") String userID, AccountProfileDataBean profileData) {
        try {
            profileData.setUserID(userID);
            AccountProfileDataBean updated = tradeService.updateAccountProfile(profileData);
            return Response.ok(updated).build();
        } catch (Exception e) {
            Log.error("TradeResource:updateAccountProfile", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    // ==================== Quote Operations ====================

    @GET
    @Path("/quotes/{symbols}")
    public Response getQuotes(@PathParam("symbols") String symbols) {
        try {
            ArrayList<QuoteDataBean> quoteDataBeans = new ArrayList<>();
            String[] symbolsSplit = symbols.split(",");
            for (String symbol : symbolsSplit) {
                QuoteDataBean quoteData = tradeService.getQuote(symbol.trim());
                if (quoteData != null) {
                    quoteDataBeans.add(quoteData);
                }
            }
            return Response.ok(quoteDataBeans).build();
        } catch (Exception e) {
            Log.error("TradeResource:getQuotes", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/quotes")
    public Response getAllQuotes() {
        try {
            Collection<?> quotes = tradeService.getAllQuotes();
            return Response.ok(quotes).build();
        } catch (Exception e) {
            Log.error("TradeResource:getAllQuotes", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    // ==================== Trading Operations ====================

    @POST
    @Path("/buy")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response buy(@FormParam("userID") String userID, @FormParam("symbol") String symbol,
                        @FormParam("quantity") double quantity) {
        try {
            OrderDataBean order = tradeService.buy(userID, symbol, quantity, TradeConfig.SYNCH);
            return Response.ok(order).build();
        } catch (Exception e) {
            Log.error("TradeResource:buy", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/sell")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response sell(@FormParam("userID") String userID, @FormParam("holdingID") Integer holdingID) {
        try {
            OrderDataBean order = tradeService.sell(userID, holdingID, TradeConfig.SYNCH);
            return Response.ok(order).build();
        } catch (Exception e) {
            Log.error("TradeResource:sell", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    // ==================== Holdings ====================

    @GET
    @Path("/holdings/{userID}")
    public Response getHoldings(@PathParam("userID") String userID) {
        try {
            Collection<HoldingDataBean> holdings = tradeService.getHoldings(userID);
            return Response.ok(holdings).build();
        } catch (Exception e) {
            Log.error("TradeResource:getHoldings", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/holding/{holdingID}")
    public Response getHolding(@PathParam("holdingID") Integer holdingID) {
        try {
            HoldingDataBean holding = tradeService.getHolding(holdingID);
            if (holding == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(holding).build();
        } catch (Exception e) {
            Log.error("TradeResource:getHolding", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    // ==================== Orders ====================

    @GET
    @Path("/orders/{userID}")
    public Response getOrders(@PathParam("userID") String userID) {
        try {
            Collection<?> orders = tradeService.getOrders(userID);
            return Response.ok(orders).build();
        } catch (Exception e) {
            Log.error("TradeResource:getOrders", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/closedOrders/{userID}")
    @Transactional
    public Response getClosedOrders(@PathParam("userID") String userID) {
        try {
            Collection<?> orders = tradeService.getClosedOrders(userID);
            return Response.ok(orders).build();
        } catch (Exception e) {
            Log.error("TradeResource:getClosedOrders", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    // ==================== Ping / Health ====================

    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok("{\"status\":\"alive\",\"framework\":\"Quarkus\",\"app\":\"DayTrader\"}").build();
    }
}
