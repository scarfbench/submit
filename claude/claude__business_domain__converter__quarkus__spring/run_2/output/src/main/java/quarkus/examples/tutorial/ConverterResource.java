package quarkus.examples.tutorial;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ConverterResource {

    @Autowired
    private ConverterBean converter;

    @Autowired
    private HttpServletRequest request;

    @GetMapping(produces = "text/html")
    public String convert(@RequestParam(required = false) String amount) {
        StringBuilder html = new StringBuilder();
        html.append("<html lang=\"en\">")
                .append("<head>")
                .append("<title>Servlet ConverterServlet</title>")
                .append("</head>")
                .append("<body>")
                .append("<h1>Servlet ConverterServlet at ").append(request.getContextPath())
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
