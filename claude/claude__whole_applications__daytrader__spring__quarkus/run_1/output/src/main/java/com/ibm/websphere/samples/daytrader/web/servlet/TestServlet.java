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
package com.ibm.websphere.samples.daytrader.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "TestServlet", urlPatterns = { "/TestServlet" })
public class TestServlet extends HttpServlet {

    private static final long serialVersionUID = -2927579146688173127L;

    private TradeServices tradeAction;

    @Inject
    @jakarta.enterprise.inject.Any
    jakarta.enterprise.inject.Instance<TradeServices> tradeServicesInstance;

    public TestServlet() {
        // Default constructor for servlet container
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Resolve TradeServices by configured runtime mode using CDI
        String key = TradeConfig.getRunTimeModeNames()[TradeConfig.getRunTimeMode()];
        for (TradeServices ts : tradeServicesInstance) {
            String beanName = getBeanName(ts);
            if (key.equals(beanName)) {
                this.tradeAction = ts;
                break;
            }
        }
        if (this.tradeAction == null) {
            List<String> available = new ArrayList<>();
            for (TradeServices ts : tradeServicesInstance) {
                available.add(getBeanName(ts));
            }
            throw new IllegalStateException("No TradeServices bean named '" + key + "' (available: " + available + ")");
        }
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
     * Process incoming HTTP GET requests
     *
     * @param request
     *                 Object that encapsulates the request to the servlet
     * @param response
     *                 Object that encapsulates the response from the servlet
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        performTask(request, response);
    }

    /**
     * Process incoming HTTP POST requests
     *
     * @param request
     *                 Object that encapsulates the request to the servlet
     * @param response
     *                 Object that encapsulates the response from the servlet
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        performTask(request, response);
    }

    /**
     * Main service method for TradeAppServlet
     *
     * @param request
     *                 Object that encapsulates the request to the servlet
     * @param response
     *                 Object that encapsulates the response from the servlet
     */
    public void performTask(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Log.debug("Enter TestServlet doGet");
            // TradeDirect tradeDirect = new TradeDirect();
            for (int i = 0; i < 10; i++) {
                tradeAction.createQuote(
                        "s:" + i,
                        "Company " + i,
                        new BigDecimal(i * 1.1));
            }
            /*
             *
             * AccountDataBean accountData = new TradeAction().register("user1",
             * "password", "fullname", "address", "email", "creditCard", new
             * BigDecimal(123.45), false);
             *
             * OrderDataBean orderData = new TradeAction().buy("user1", "s:1",
             * 100.0); orderData = new TradeAction().buy("user1", "s:2", 200.0);
             * Thread.sleep(5000); accountData = new
             * TradeAction().getAccountData("user1"); Collection
             * holdingDataBeans = new TradeAction().getHoldings("user1");
             * PrintWriter out = resp.getWriter();
             * resp.setContentType("text/html");
             * out.write("<HEAD></HEAD><BODY><BR><BR>");
             * out.write(accountData.toString());
             * Log.printCollection("user1 Holdings", holdingDataBeans);
             * ServletContext sc = getServletContext();
             * req.setAttribute("results", "Success");
             * req.setAttribute("accountData", accountData);
             * req.setAttribute("holdingDataBeans", holdingDataBeans);
             * getServletContext
             * ().getRequestDispatcher("/tradehome.jsp").include(req, resp);
             * out.write("<BR><BR>done.</BODY>");
             */
        } catch (Exception e) {
            Log.error("TestServletException", e);
        }
    }
}
