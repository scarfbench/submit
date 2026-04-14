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
package com.ibm.websphere.samples.daytrader.events;

import java.math.BigDecimal;

public class QuoteUpdateEvent {
    private final String symbol;
    private final String companyName;
    private final BigDecimal price;
    private final BigDecimal oldPrice;
    private final BigDecimal open;
    private final BigDecimal low;
    private final BigDecimal high;
    private final double volume;
    private final BigDecimal changeFactor;
    private final double sharesTraded;
    private final long publishTime;

    public QuoteUpdateEvent(String symbol, String companyName, BigDecimal price,
                           BigDecimal oldPrice, BigDecimal open, BigDecimal low,
                           BigDecimal high, double volume, BigDecimal changeFactor,
                           double sharesTraded, long publishTime) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.price = price;
        this.oldPrice = oldPrice;
        this.open = open;
        this.low = low;
        this.high = high;
        this.volume = volume;
        this.changeFactor = changeFactor;
        this.sharesTraded = sharesTraded;
        this.publishTime = publishTime;
    }

    public String getSymbol() { return symbol; }
    public String getCompanyName() { return companyName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public BigDecimal getOpen() { return open; }
    public BigDecimal getLow() { return low; }
    public BigDecimal getHigh() { return high; }
    public double getVolume() { return volume; }
    public BigDecimal getChangeFactor() { return changeFactor; }
    public double getSharesTraded() { return sharesTraded; }
    public long getPublishTime() { return publishTime; }
}
