package com.ibm.websphere.samples.daytrader.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.service.TradeServiceImpl;

@Path("/rest/quotes")
@Produces(MediaType.APPLICATION_JSON)
public class QuoteResource {

    @Inject
    TradeServiceImpl tradeService;

    @GET
    @Path("/{symbols}")
    public Response getQuotes(@PathParam("symbols") String symbols) {
        try {
            List<QuoteDataBean> quotes = new ArrayList<>();
            String[] symbolsSplit = symbols.split(",");
            for (String symbol : symbolsSplit) {
                QuoteDataBean quote = tradeService.getQuote(symbol.trim());
                if (quote != null) {
                    quotes.add(quote);
                }
            }
            return Response.ok(quotes).build();
        } catch (Exception e) {
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postQuotes(@FormParam("symbols") String symbols) {
        return getQuotes(symbols);
    }

    @GET
    @Path("/all")
    public Response getAllQuotes() {
        try {
            Collection<QuoteDataBean> quotes = tradeService.getAllQuotes();
            return Response.ok(quotes).build();
        } catch (Exception e) {
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
