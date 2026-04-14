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
package com.ibm.websphere.samples.daytrader.web;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/app")
public class TradeAppResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String home() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html><head><title>DayTrader</title></head>\n");
        sb.append("<body>\n");
        sb.append("<h1>DayTrader Jakarta EE</h1>\n");
        sb.append("<p>Welcome to DayTrader - a Jakarta EE benchmark application.</p>\n");
        sb.append("<h2>REST API Endpoints</h2>\n");
        sb.append("<ul>\n");
        sb.append("<li><a href=\"/rest/trade/market\">Market Summary</a></li>\n");
        sb.append("<li><a href=\"/rest/quotes/s:0\">Get Quote (s:0)</a></li>\n");
        sb.append("<li><a href=\"/rest/quotes/s:0,s:1,s:2\">Get Multiple Quotes</a></li>\n");
        sb.append("<li><a href=\"/rest/trade/account/uid:0\">Account Data (uid:0)</a></li>\n");
        sb.append("<li><a href=\"/rest/trade/account/uid:0/profile\">Account Profile (uid:0)</a></li>\n");
        sb.append("<li><a href=\"/rest/trade/account/uid:0/holdings\">Holdings (uid:0)</a></li>\n");
        sb.append("<li><a href=\"/rest/trade/account/uid:0/orders\">Orders (uid:0)</a></li>\n");
        sb.append("<li><a href=\"/rest/trade/resetTrade\">Reset Trade</a></li>\n");
        sb.append("</ul>\n");
        sb.append("<h2>Configuration</h2>\n");
        sb.append("<p>Runtime Mode: EJB3 (JPA)</p>\n");
        sb.append("<p>Database: Derby (In-Memory)</p>\n");
        sb.append("</body></html>");
        return sb.toString();
    }
}
