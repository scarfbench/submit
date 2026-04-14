/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.converter.web;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.tutorial.converter.ejb.ConverterBean;

/**
 * Spring MVC Controller for currency conversion.
 * Migrated from Jakarta Servlet to Spring Controller.
 */
@Controller
public class ConverterServlet {

    @Autowired
    private ConverterBean converter;

    /**
     * Processes requests for both HTTP GET and POST methods.
     * @param amount the dollar amount to convert (optional)
     * @return HTML response with conversion results or input form
     */
    @GetMapping("/")
    @ResponseBody
    public String processRequest(@RequestParam(required = false) String amount) {
        StringBuilder html = new StringBuilder();
        html.append("<html lang=\"en\">");
        html.append("<head>");
        html.append("<title>Converter Application</title>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>Currency Converter</h1>");

        if (amount != null && amount.length() > 0) {
            try {
                // convert the amount to a BigDecimal from the request parameter
                BigDecimal d = new BigDecimal(amount);
                // call the ConverterBean.dollarToYen() method to get the amount in Yen
                BigDecimal yenAmount = converter.dollarToYen(d);

                // call the ConverterBean.yenToEuro() method to get the amount in Euros
                BigDecimal euroAmount = converter.yenToEuro(yenAmount);

                html.append("<p>").append(amount).append(" dollars are ")
                    .append(yenAmount.toPlainString()).append(" yen.</p>");
                html.append("<p>").append(yenAmount.toPlainString()).append(" yen are ")
                    .append(euroAmount.toPlainString()).append(" Euro.</p>");
            } catch (NumberFormatException e) {
                html.append("<p style=\"color:red;\">Invalid amount format. Please enter a valid number.</p>");
            }
        } else {
            html.append("<p>Enter a dollar amount to convert:</p>");
            html.append("<form method=\"get\">");
            html.append("<p>$ <input title=\"Amount\" type=\"text\" name=\"amount\" size=\"25\"></p>");
            html.append("<br/>");
            html.append("<input type=\"submit\" value=\"Submit\">");
            html.append("<input type=\"reset\" value=\"Reset\">");
            html.append("</form>");
        }

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
