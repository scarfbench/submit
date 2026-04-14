package spring.tutorial.web.dukeetf2;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DukeEtfApplication implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("DukeETF Application initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("DukeETF Application destroyed");
    }
}