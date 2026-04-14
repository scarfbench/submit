package jakarta.tutorial.web.dukeetf2;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DukeEtfApplication implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Application initialization logic
        System.out.println("Duke ETF Application initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Application cleanup logic
        System.out.println("Duke ETF Application destroyed");
    }
}
