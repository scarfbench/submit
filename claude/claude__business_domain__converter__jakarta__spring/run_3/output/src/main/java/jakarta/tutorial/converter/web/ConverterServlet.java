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

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.tutorial.converter.ejb.ConverterBean;

@Controller
public class ConverterServlet {

    @Autowired
    private ConverterBean converter;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param amount the amount parameter from the request
     * @param request servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/")
    @PostMapping("/")
    public void processRequest(@RequestParam(value = "amount", required = false) String amount,
                               HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // Output the results
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<title>Servlet ConverterServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet ConverterServlet at " +
                request.getContextPath() + "</h1>");
        try {
            if (amount != null && amount.length() > 0) {
                // convert the amount to a BigDecimal from the request parameter
                BigDecimal d = new BigDecimal(amount);
                // call the ConverterBean.dollarToYen() method to get the amount
                // in Yen
                BigDecimal yenAmount = converter.dollarToYen(d);

                // call the ConverterBean.yenToEuro() method to get the amount
                // in Euros
                BigDecimal euroAmount = converter.yenToEuro(yenAmount);

                out.println("<p>" + amount + " dollars are " +
                        yenAmount.toPlainString() + " yen.</p>");
                out.println("<p>" + yenAmount.toPlainString() + " yen are " +
                        euroAmount.toPlainString() + " Euro.</p>");
            } else {
                out.println("<p>Enter a dollar amount to convert:</p>");
                out.println("<form method=\"get\">");
                out.println("<p>$ <input title=\"Amount\" type=\"text\" name=\"amount\" size=\"25\"></p>");
                out.println("<br/>");
                out.println("<input type=\"submit\" value=\"Submit\">" +
                        "<input type=\"reset\" value=\"Reset\">");
                out.println("</form>");
            }

        } finally {
            out.println("</body>");
            out.println("</html>");
            out.close();
        }
    }
}
