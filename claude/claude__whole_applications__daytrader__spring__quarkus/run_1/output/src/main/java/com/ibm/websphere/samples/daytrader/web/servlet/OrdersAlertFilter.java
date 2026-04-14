/**
 * (C) Copyright IBM Corporation 2015, 2022.
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
package com.ibm.websphere.samples.daytrader.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;

import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Diagnostics;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

@WebFilter(filterName = "OrdersAlertFilter", urlPatterns = "/app")
@Trace
public class OrdersAlertFilter implements Filter {

    @Inject
    @jakarta.enterprise.inject.Any
    jakarta.enterprise.inject.Instance<TradeServices> tradeServicesInstance;

    public OrdersAlertFilter() {
        // Default constructor for servlet container
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    private FilterConfig filterConfig = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    private TradeServices getTradeAction() {
        String key = TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()];
        for (TradeServices ts : tradeServicesInstance) {
            String beanName = getBeanName(ts);
            if (key.equals(beanName)) {
                return ts;
            }
        }
        return tradeServicesInstance.iterator().next();
    }

    private String getBeanName(TradeServices ts) {
        // CDI proxy classes need getSuperclass() to find the actual class with annotations
        Class<?> clazz = ts.getClass();
        jakarta.inject.Named named = clazz.getAnnotation(jakarta.inject.Named.class);
        if (named == null && clazz.getSuperclass() != null) {
            named = clazz.getSuperclass().getAnnotation(jakarta.inject.Named.class);
        }
        if (named != null && !named.value().isEmpty()) {
            return named.value();
        }
        // Check superclass for qualifier annotations too
        Class<?> realClass = clazz;
        if (realClass.getSuperclass() != null && realClass.getSuperclass() != Object.class) {
            realClass = realClass.getSuperclass();
        }
        if (realClass.getAnnotation(com.ibm.websphere.samples.daytrader.interfaces.TradeJDBC.class) != null) {
            return "Direct (JDBC)";
        }
        if (realClass.getAnnotation(com.ibm.websphere.samples.daytrader.interfaces.TradeEJB.class) != null) {
            return "Full EJB3";
        }
        if (realClass.getAnnotation(com.ibm.websphere.samples.daytrader.interfaces.TradeSession2Direct.class) != null) {
            return "Session to Direct";
        }
        // Default to simple class name
        String simpleName = realClass.getSimpleName();
        return simpleName;
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(
            ServletRequest req,
            ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        if (filterConfig == null) {
            return;
        }

        if (TradeConfig.getDisplayOrderAlerts() == true) {
            try {
                String action = req.getParameter("action");
                if (action != null) {
                    action = action.trim();
                    if ((action.length() > 0) && (!action.equals("logout"))) {
                        String userID;
                        if (action.equals("login")) {
                            userID = req.getParameter("uid");
                        } else {
                            userID = (String) ((HttpServletRequest) req).getSession().getAttribute(
                                    "uidBean");
                        }

                        if ((userID != null) && (userID.trim().length() > 0)) {
                            Collection<?> closedOrders = getTradeAction().getClosedOrders(userID);
                            if ((closedOrders != null) &&
                                    (closedOrders.size() > 0)) {
                                req.setAttribute("closedOrders", closedOrders);
                            }
                            if (Log.doTrace()) {
                                Log.printCollection(
                                        "OrderAlertFilter: userID=" +
                                                userID +
                                                " closedOrders=",
                                        closedOrders);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.error(
                        e,
                        "OrdersAlertFilter - Error checking for closedOrders");
            }
        }

        Diagnostics.checkDiagnostics();

        chain.doFilter(req, resp /* wrapper */);
    }

    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}
