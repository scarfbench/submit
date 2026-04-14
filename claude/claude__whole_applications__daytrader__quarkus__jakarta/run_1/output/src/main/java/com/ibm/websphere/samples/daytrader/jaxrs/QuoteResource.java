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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.util.TradeRunTimeModeLiteral;

@Path("/quotes")
@Produces(MediaType.APPLICATION_JSON)
public class QuoteResource {

    @Inject
    @Any
    Instance<TradeServices> tradeServiceInstances;

    private TradeServices getTradeServices() {
        String mode = TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()];
        return tradeServiceInstances.select(new TradeRunTimeModeLiteral(mode)).get();
    }

    @GET
    @Path("/{symbols}")
    public Response getQuotes(@PathParam("symbols") String symbols) {
        try {
            List<QuoteDataBean> quotes = new ArrayList<>();
            String[] symbolArray = symbols.split(",");
            for (String symbol : symbolArray) {
                symbol = symbol.trim();
                if (!symbol.isEmpty()) {
                    QuoteDataBean quote = getTradeServices().getQuote(symbol);
                    if (quote != null) {
                        quotes.add(quote);
                    }
                }
            }
            return Response.ok(quotes).build();
        } catch (Exception e) {
            Log.error("QuoteResource:getQuotes", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllQuotes() {
        try {
            Collection<?> quotes = getTradeServices().getAllQuotes();
            return Response.ok(quotes).build();
        } catch (Exception e) {
            Log.error("QuoteResource:getAllQuotes", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
