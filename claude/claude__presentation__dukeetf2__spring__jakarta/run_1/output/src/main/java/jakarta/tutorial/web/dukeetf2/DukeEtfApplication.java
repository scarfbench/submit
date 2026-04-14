package jakarta.tutorial.web.dukeetf2;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class DukeEtfApplication implements ServletContextListener {

    private static ScheduledExecutorService scheduler;

    public static void main(String[] args) throws LifecycleException {
        String webappDirLocation = "src/main/webapp/";
        Tomcat tomcat = new Tomcat();

        // Set port
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }
        tomcat.setPort(Integer.parseInt(webPort));

        // Configure context
        Context context = tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());

        // Add resources
        File additionWebInfClasses = new File("target/classes");
        StandardRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        context.setResources(resources);

        tomcat.start();
        tomcat.getServer().await();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialize scheduler
        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}