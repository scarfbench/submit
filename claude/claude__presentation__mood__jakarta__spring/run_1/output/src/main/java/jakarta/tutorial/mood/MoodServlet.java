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
package jakarta.tutorial.mood;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class MoodServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     *  methods.
     * @param request servlet request
     * @param response servlet response
     * @param mood the mood attribute set by the filter
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response, String mood)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        var out = response.getWriter();
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<title>Servlet MoodServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet MoodServlet at "
                + request.getContextPath() + "</h1>");
        out.println("<p>Duke's mood is: " + mood + "</p>");
        switch (mood) {
            case "sleepy":
                out.println("<img src=\"resources/images/duke.snooze.gif\" alt=\"Duke sleeping\"/><br/>");
                break;
            case "alert":
                out.println("<img src=\"resources/images/duke.waving.gif\" alt=\"Duke waving\"/><br/>");
                break;
            case "hungry":
                out.println("<img src=\"resources/images/duke.cookies.gif\" alt=\"Duke with cookies\"/><br/>");
                break;
            case "lethargic":
                out.println("<img src=\"resources/images/duke.handsOnHips.gif\" alt=\"Duke with hands on hips\"/><br/>");
                break;
            case "thoughtful":
                out.println("<img src=\"resources/images/duke.pensive.gif\" alt=\"Duke thinking\"/><br/>");
                break;
            default:
                out.println("<img src=\"resources/images/duke.thumbsup.gif\" alt=\"Duke with thumbs-up gesture\"/><br/>");
                break;
        }
        out.println("</body>");
        out.println("</html>");
        out.flush();
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @param mood the mood attribute set by the filter
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/report")
    protected void doGet(HttpServletRequest request, HttpServletResponse response,
            @RequestAttribute(name = "mood", required = false) String mood)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        if (mood == null) {
            mood = "awake";
        }
        processRequest(request, response, mood);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @param mood the mood attribute set by the filter
     * @throws IOException if an I/O error occurs
     */
    @PostMapping("/report")
    protected void doPost(HttpServletRequest request, HttpServletResponse response,
            @RequestAttribute(name = "mood", required = false) String mood)
            throws IOException {
        if (mood == null) {
            mood = "awake";
        }
        processRequest(request, response, mood);
    }
}
