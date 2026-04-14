/**
 * (C) Copyright IBM Corporation 2019.
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
package com.ibm.websphere.samples.daytrader.web.websocket;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;


/** This class takes a list of quotedata (from the RecentQuotePriceChangeList bean) and encodes
   it to the json format the client (marektsummary.html) is expecting. **/
public class QuotePriceChangeListEncoder implements Encoder.Text<CopyOnWriteArrayList<QuoteDataBean>> {

  public String encode(CopyOnWriteArrayList<QuoteDataBean> list) throws EncodeException {

    StringBuilder json = new StringBuilder();
    json.append("{");

    int i = 1;
    boolean first = true;

    for (Iterator<QuoteDataBean> iterator = list.iterator(); iterator.hasNext();) {
      QuoteDataBean quotedata = iterator.next();

      if (!first) {
        json.append(",");
      }
      first = false;

      json.append("\"change").append(i).append("_stock\":\"").append(escapeJson(quotedata.getSymbol())).append("\",");
      json.append("\"change").append(i).append("_price\":\"$").append(quotedata.getPrice()).append("\",");
      json.append("\"change").append(i).append("_change\":").append(quotedata.getChange());
      i++;
    }

    json.append("}");
    return json.toString();
  }

  private String escapeJson(String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\")
               .replace("\"", "\\\"")
               .replace("\n", "\\n")
               .replace("\r", "\\r")
               .replace("\t", "\\t");
  }

  @Override
  public void init(EndpointConfig config) {
    // No initialization needed
  }

  @Override
  public void destroy() {
    // No cleanup needed
  }

}
