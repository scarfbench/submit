package com.ibm.websphere.samples.daytrader.rest;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean;
import com.ibm.websphere.samples.daytrader.service.TradeServiceImpl;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Path("/rest/config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

    @Inject
    TradeServiceImpl tradeService;

    private Map<String, String> errorMap(Exception e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        return map;
    }

    @GET
    public Response getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxUsers", TradeConfig.getMAX_USERS());
        config.put("maxQuotes", TradeConfig.getMAX_QUOTES());
        config.put("maxHoldings", TradeConfig.getMAX_HOLDINGS());
        config.put("marketSummaryInterval", TradeConfig.getMarketSummaryInterval());
        config.put("updateQuotePrices", TradeConfig.getUpdateQuotePrices());
        config.put("runtimeMode", TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()]);
        config.put("status", "running");
        return Response.ok(config).build();
    }

    @POST
    @Path("/populate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response populateDatabase(Map<String, Integer> params) {
        try {
            int maxUsers = params.getOrDefault("maxUsers", 50);
            int maxQuotes = params.getOrDefault("maxQuotes", 100);

            TradeConfig.setMAX_USERS(maxUsers);
            TradeConfig.setMAX_QUOTES(maxQuotes);

            // First reset
            tradeService.resetTrade(true);

            // Populate
            tradeService.populateDatabase(maxUsers, maxQuotes);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "populated");
            result.put("maxUsers", maxUsers);
            result.put("maxQuotes", maxQuotes);
            return Response.ok(result).build();
        } catch (Exception e) {
            Log.error("Error populating database", e);
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @POST
    @Path("/reset")
    public Response resetTrade() {
        try {
            RunStatsDataBean stats = tradeService.resetTrade(true);
            return Response.ok(stats).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }

    @GET
    @Path("/stats")
    public Response getStats() {
        try {
            RunStatsDataBean stats = tradeService.resetTrade(false);
            return Response.ok(stats).build();
        } catch (Exception e) {
            return Response.serverError().entity(errorMap(e)).build();
        }
    }
}
