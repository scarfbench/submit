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
package com.ibm.websphere.samples.daytrader.web.prims.ejb3;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

/**
 * This primitive is designed to run inside the TradeApplication and relies upon
 * the {@link com.ibm.websphere.samples.daytrader.util.TradeConfig} class to set
 * configuration parameters. PingServlet2MDBTopic tests key functionality of a
 * servlet call to post a message to an MDB Topic.
 *
 * NOTE: JMS functionality removed for Spring Boot migration as JMS/MDB is not available.
 * This servlet now just logs the ping request.
 */
@WebServlet(name = "ejb3.PingServlet2MDBTopic", urlPatterns = { "/ejb3/PingServlet2MDBTopic" })
public class PingServlet2MDBTopic extends HttpServlet {

    private static final long serialVersionUID = 5925470158886928225L;

    private static String initTime;

    private static int hitCount;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        res.setContentType("text/html");
        java.io.PrintWriter out = res.getWriter();

        StringBuffer output = new StringBuffer(100);
        output.append("<html><head><title>PingServlet2MDBTopic</title></head>"
                + "<body><HR><FONT size=\"+2\" color=\"#000066\">PingServlet2MDBTopic<BR></FONT>" + "<FONT size=\"-1\" color=\"#000066\">"
                + "Simulates posting a message to an MDB Topic (JMS not available in Spring Boot).<BR>"
                + "<FONT color=\"red\"><B>Note:</B> Not intended for performance testing.</FONT>");

        try {
            int iter = TradeConfig.getPrimIterations();
            for (int ii = 0; ii < iter; ii++) {
                // Log the simulated message
                Log.log("PingServlet2MDBTopic: Simulated ping message #" + ii + " at " + new java.util.Date());
            }

            // write out the output
            output.append("<HR>initTime: ").append(initTime);
            output.append("<BR>Hit Count: ").append(hitCount++);
            output.append("<HR>Simulated posting of messages to MDB Topic (JMS not available in Spring Boot)");
            output.append("<BR>Iterations: ").append(iter);
            output.append("<BR><HR></FONT></BODY></HTML>");
            out.println(output.toString());

        } catch (Exception e) {
            Log.error(e, "PingServlet2MDBTopic.doGet(...): error");
            res.sendError(500, "PingServlet2MDBTopic.doGet(...): error, " + e.toString());
        }
    }

    @Override
    public String getServletInfo() {
        return "web primitive, configured with trade runtime configs, simulates Servlet to MDB Topic";
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        hitCount = 0;
        initTime = new java.util.Date().toString();
    }

}
