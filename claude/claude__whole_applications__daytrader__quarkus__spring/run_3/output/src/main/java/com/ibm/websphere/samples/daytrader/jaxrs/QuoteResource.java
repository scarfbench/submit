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
package com.ibm.websphere.samples.daytrader.jaxrs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;

/**
 * MIGRATION NOTES:
 *
 * 1. Migrated from JAX-RS to Spring MVC
 * 2. Simplified dependency injection using @Autowired
 * 3. Using @RestController for automatic JSON serialization
 */
@RestController
@RequestMapping("/quotes")
public class QuoteResource {

    @Autowired
    TradeServices tradeService;

    @GetMapping("/{symbols}")
    public List<QuoteDataBean> quotesGet(@PathVariable("symbols") String symbols) {
        return getQuotes(symbols);
    }

    @PostMapping
    public List<QuoteDataBean> quotesPost(@RequestParam("symbols") String symbols) {
        return getQuotes(symbols);
    }

    private List<QuoteDataBean> getQuotes(String symbols) {
        ArrayList<QuoteDataBean> quoteDataBeans = new ArrayList<>();

        try {
            String[] symbolsSplit = symbols.split(",");
            for (String symbol : symbolsSplit) {
                QuoteDataBean quoteData = tradeService.getQuote(symbol.trim());
                if (quoteData != null) {
                    quoteDataBeans.add(quoteData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quoteDataBeans;
    }
}
