package spring.examples.tutorial.converter.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import spring.examples.tutorial.converter.service.ConverterService;

import java.math.BigDecimal;

/**
 * Jakarta REST (JAX-RS) Resource for currency conversion
 */
@Path("/")
public class ConverterController {

    @Inject
    private ConverterService converter;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String convert(@QueryParam("amount") String amount,
                         @Context HttpServletRequest request) {
        StringBuilder html = new StringBuilder();
        html.append("<html lang=\"en\">")
                .append("<head>")
                .append("<title>Jakarta EE Converter</title>")
                .append("</head>")
                .append("<body>")
                .append("<h1>Jakarta EE Converter at ").append(request.getContextPath())
                .append("</h1>");

        try {
            if (amount != null && !amount.isEmpty()) {
                BigDecimal d = new BigDecimal(amount);
                BigDecimal yenAmount = converter.dollarToYen(d);
                BigDecimal euroAmount = converter.yenToEuro(yenAmount);

                html.append("<p>").append(amount).append(" dollars are ")
                        .append(yenAmount.toPlainString()).append(" yen.</p>");
                html.append("<p>").append(yenAmount.toPlainString()).append(" yen are ")
                        .append(euroAmount.toPlainString()).append(" Euro.</p>");
            } else {
                html.append("<p>Enter a dollar amount to convert:</p>")
                        .append("<form method=\"get\">")
                        .append("<p>$ <input title=\"Amount\" type=\"text\" name=\"amount\" size=\"25\"></p>")
                        .append("<br/>")
                        .append("<input type=\"submit\" value=\"Submit\">")
                        .append("<input type=\"reset\" value=\"Reset\">")
                        .append("</form>");
            }
        } finally {
            html.append("</body>")
                    .append("</html>");
        }

        return html.toString();
    }
}
