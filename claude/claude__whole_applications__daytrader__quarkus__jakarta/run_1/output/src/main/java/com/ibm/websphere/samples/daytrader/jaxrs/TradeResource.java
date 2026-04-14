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
package com.ibm.websphere.samples.daytrader.jaxrs;

import java.math.BigDecimal;
import java.util.Collection;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.impl.direct.TradeDirectDBUtils;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.util.TradeRunTimeModeLiteral;

@Path("/trade")
@Produces(MediaType.APPLICATION_JSON)
public class TradeResource {

    @Inject
    @Any
    Instance<TradeServices> tradeServiceInstances;

    @Inject
    TradeDirectDBUtils dbUtils;

    private TradeServices getTradeServices() {
        String mode = TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()];
        TradeServices ts = tradeServiceInstances.select(new TradeRunTimeModeLiteral(mode)).get();
        return ts;
    }

    @GET
    @Path("/market")
    public Response getMarketSummary() {
        try {
            MarketSummaryDataBean summary = getTradeServices().getMarketSummary();
            return Response.ok(summary).build();
        } catch (Exception e) {
            Log.error("TradeResource:getMarketSummary", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/buy")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response buy(@FormParam("userID") String userID, @FormParam("symbol") String symbol,
            @FormParam("quantity") String quantity) {
        try {
            double qty = Double.parseDouble(quantity);
            OrderDataBean order = getTradeServices().buy(userID, symbol, qty, TradeConfig.getOrderProcessingMode());
            return Response.ok(order).build();
        } catch (Exception e) {
            Log.error("TradeResource:buy", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/sell")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sell(@FormParam("userID") String userID, @FormParam("holdingID") String holdingID) {
        try {
            int hID = Integer.parseInt(holdingID);
            OrderDataBean order = getTradeServices().sell(userID, hID, TradeConfig.getOrderProcessingMode());
            return Response.ok(order).build();
        } catch (Exception e) {
            Log.error("TradeResource:sell", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("userID") String userID, @FormParam("password") String password) {
        try {
            AccountDataBean account = getTradeServices().login(userID, password);
            return Response.ok(account).build();
        } catch (Exception e) {
            Log.error("TradeResource:login", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/logout/{userID}")
    public Response logout(@PathParam("userID") String userID) {
        try {
            getTradeServices().logout(userID);
            return Response.ok("{\"status\":\"logged out\"}").build();
        } catch (Exception e) {
            Log.error("TradeResource:logout", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/account/{userID}")
    public Response getAccountData(@PathParam("userID") String userID) {
        try {
            AccountDataBean account = getTradeServices().getAccountData(userID);
            return Response.ok(account).build();
        } catch (Exception e) {
            Log.error("TradeResource:getAccountData", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/account/{userID}/profile")
    public Response getAccountProfileData(@PathParam("userID") String userID) {
        try {
            AccountProfileDataBean profile = getTradeServices().getAccountProfileData(userID);
            return Response.ok(profile).build();
        } catch (Exception e) {
            Log.error("TradeResource:getAccountProfileData", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/account/{userID}/holdings")
    public Response getHoldings(@PathParam("userID") String userID) {
        try {
            Collection<HoldingDataBean> holdings = getTradeServices().getHoldings(userID);
            return Response.ok(holdings).build();
        } catch (Exception e) {
            Log.error("TradeResource:getHoldings", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/account/{userID}/orders")
    public Response getOrders(@PathParam("userID") String userID) {
        try {
            Collection<?> orders = getTradeServices().getOrders(userID);
            return Response.ok(orders).build();
        } catch (Exception e) {
            Log.error("TradeResource:getOrders", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response register(@FormParam("userID") String userID, @FormParam("password") String password,
            @FormParam("fullname") String fullname, @FormParam("address") String address,
            @FormParam("email") String email, @FormParam("creditcard") String creditcard,
            @FormParam("openBalance") String openBalance) {
        try {
            BigDecimal balance = new BigDecimal(openBalance);
            AccountDataBean account = getTradeServices().register(userID, password, fullname, address, email,
                    creditcard, balance);
            return Response.ok(account).build();
        } catch (Exception e) {
            Log.error("TradeResource:register", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/resetTrade")
    public Response resetTrade() {
        try {
            RunStatsDataBean stats = dbUtils.resetTrade(false);
            return Response.ok(stats).build();
        } catch (Exception e) {
            Log.error("TradeResource:resetTrade", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
