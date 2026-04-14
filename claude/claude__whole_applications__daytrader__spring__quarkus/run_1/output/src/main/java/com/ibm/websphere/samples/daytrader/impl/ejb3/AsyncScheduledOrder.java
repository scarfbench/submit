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
package com.ibm.websphere.samples.daytrader.impl.ejb3;

import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

@Dependent
public class AsyncScheduledOrder implements Runnable {

    @Inject
    @Any
    Instance<TradeServices> tradeServicesInstance;

    Integer orderID;
    boolean twoPhase;

    public void setProperties(Integer orderID, boolean twoPhase) {
        this.orderID = orderID;
        this.twoPhase = twoPhase;
    }

    private TradeServices getTradeService() {
        String key = TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()];
        for (TradeServices ts : tradeServicesInstance) {
            Class<?> clazz = ts.getClass();
            Named named = clazz.getAnnotation(Named.class);
            if (named == null && clazz.getSuperclass() != null) {
                named = clazz.getSuperclass().getAnnotation(Named.class);
            }
            if (named != null && key.equals(named.value())) {
                return ts;
            }
        }
        // Fallback: return first available
        return tradeServicesInstance.iterator().next();
    }

    @Override
    public void run() {
        try {
            getTradeService().completeOrder(orderID, twoPhase);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
