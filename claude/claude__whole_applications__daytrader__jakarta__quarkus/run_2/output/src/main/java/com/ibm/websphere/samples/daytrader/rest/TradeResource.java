package com.ibm.websphere.samples.daytrader.rest;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.service.TradeServiceImpl;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Path("/rest/trade")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TradeResource {

    @Inject
    TradeServiceImpl tradeService;

    private Map<String, String> errorMap(Exception e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        return map;
    }

    @GET
    @Path("/marketSummary")
    public Response getMarketSummary() {
        try {
            MarketSummaryDataBean summary = tradeService.getMarketSummary();
            return Response.ok(summary).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @POST
    @Path("/buy")
    public Response buy(Map<String, Object> request) {
        try {
            String userID = (String) request.get("userID");
            String symbol = (String) request.get("symbol");
            double quantity = ((Number) request.get("quantity")).doubleValue();
            OrderDataBean order = tradeService.buy(userID, symbol, quantity, TradeConfig.SYNCH);
            return Response.ok(order).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @POST
    @Path("/sell")
    public Response sell(Map<String, Object> request) {
        try {
            String userID = (String) request.get("userID");
            Integer holdingID = ((Number) request.get("holdingID")).intValue();
            OrderDataBean order = tradeService.sell(userID, holdingID, TradeConfig.SYNCH);
            return Response.ok(order).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @GET
    @Path("/orders/{userID}")
    public Response getOrders(@PathParam("userID") String userID) {
        try {
            Collection<OrderDataBean> orders = tradeService.getOrders(userID);
            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @GET
    @Path("/closedOrders/{userID}")
    public Response getClosedOrders(@PathParam("userID") String userID) {
        try {
            Collection<OrderDataBean> orders = tradeService.getClosedOrders(userID);
            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @GET
    @Path("/holdings/{userID}")
    public Response getHoldings(@PathParam("userID") String userID) {
        try {
            Collection<HoldingDataBean> holdings = tradeService.getHoldings(userID);
            return Response.ok(holdings).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @GET
    @Path("/account/{userID}")
    public Response getAccount(@PathParam("userID") String userID) {
        try {
            AccountDataBean account = tradeService.getAccountData(userID);
            if (account == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Account not found")).build();
            }
            return Response.ok(account).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @GET
    @Path("/profile/{userID}")
    public Response getProfile(@PathParam("userID") String userID) {
        try {
            AccountProfileDataBean profile = tradeService.getAccountProfileData(userID);
            if (profile == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Profile not found")).build();
            }
            return Response.ok(profile).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @POST
    @Path("/login")
    public Response login(Map<String, String> credentials) {
        try {
            String userID = credentials.get("userID");
            String password = credentials.get("password");
            AccountDataBean account = tradeService.login(userID, password);
            return Response.ok(account).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorMap(e)).build();
        }
    }

    @POST
    @Path("/logout/{userID}")
    public Response logout(@PathParam("userID") String userID) {
        try {
            tradeService.logout(userID);
            return Response.ok(Map.of("status", "logged out")).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @POST
    @Path("/register")
    public Response register(Map<String, String> request) {
        try {
            String userID = request.get("userID");
            String password = request.get("password");
            String fullname = request.get("fullname");
            String address = request.get("address");
            String email = request.get("email");
            String creditcard = request.get("creditcard");
            String openBalanceStr = request.getOrDefault("openBalance", "1000000");
            BigDecimal openBalance = new BigDecimal(openBalanceStr);

            AccountDataBean account = tradeService.register(userID, password, fullname, address, email, creditcard, openBalance);
            return Response.ok(account).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }
}
