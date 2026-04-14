package org.example.realworldapi;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.core.Application;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.spi.ResteasyDeployment;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.weld.environment.servlet.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOG.info("Starting RealWorld API application...");

        // Initialize Weld CDI container
        SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        SeContainer container = initializer.initialize();

        // Initialize JPA
        EntityManagerFactory emf = container.select(EntityManagerFactory.class).get();
        LOG.info("EntityManagerFactory initialized: {}", emf != null);

        // Setup RESTEasy with Undertow
        UndertowJaxrsServer server = new UndertowJaxrsServer();

        ResteasyDeployment deployment = new ResteasyDeploymentImpl();
        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        deployment.setApplicationClass(RealWorldApplication.class.getName());

        DeploymentInfo di = server.undertowDeployment(deployment, "/api");
        di.setClassLoader(Main.class.getClassLoader());
        di.setContextPath("/");
        di.setDeploymentName("RealWorldAPI");
        di.addListener(Servlets.listener(Listener.class));

        int port = Integer.parseInt(System.getProperty("server.port", "8080"));

        server.deploy(di);
        server.start(
            Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
        );

        LOG.info("RealWorld API started on port {}", port);
        LOG.info("API available at http://0.0.0.0:{}/api", port);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down...");
            server.stop();
            container.close();
        }));
    }
}
