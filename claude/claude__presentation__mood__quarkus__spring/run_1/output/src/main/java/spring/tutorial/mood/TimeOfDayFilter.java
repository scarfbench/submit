package spring.tutorial.mood;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Component
@Order(1)
public class TimeOfDayFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String mood = "awake";
        Calendar cal = GregorianCalendar.getInstance();
        switch (cal.get(Calendar.HOUR_OF_DAY)) {
            case 23:
            case 24:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                mood = "sleepy";
                break;
            case 7:
            case 13:
            case 18:
                mood = "hungry";
                break;
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 16:
            case 17:
                mood = "alert";
                break;
            case 11:
            case 15:
                mood = "in need of coffee";
                break;
            case 19:
            case 20:
            case 21:
                mood = "thoughtful";
                break;
            case 22:
                mood = "lethargic";
                break;
        }

        if (request instanceof HttpServletRequest) {
            request.setAttribute("mood", mood);
        }

        chain.doFilter(request, response);
    }
}
