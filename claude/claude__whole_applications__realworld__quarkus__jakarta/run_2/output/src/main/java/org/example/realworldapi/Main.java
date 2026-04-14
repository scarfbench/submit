package org.example.realworldapi;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import jakarta.enterprise.inject.spi.BeanManager;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String host = System.getenv().getOrDefault("HOST", "0.0.0.0");

        // Initialize Weld CDI container
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        LOG.info("CDI container initialized");

        // Verify JPA is ready by touching the EntityManagerFactory producer
        container.select(
            org.example.realworldapi.infrastructure.persistence.EntityManagerProducer.class).get();
        LOG.info("JPA EntityManager ready");

        // Configure RESTEasy with CDI
        ResteasyDeployment deployment = new ResteasyDeploymentImpl();
        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());

        // Register JAX-RS resource classes
        deployment.setScannedResourceClasses(List.of(
            "org.example.realworldapi.application.web.resource.HealthResource",
            "org.example.realworldapi.application.web.resource.ArticlesResource",
            "org.example.realworldapi.application.web.resource.ProfilesResource",
            "org.example.realworldapi.application.web.resource.TagsResource",
            "org.example.realworldapi.application.web.resource.UserResource",
            "org.example.realworldapi.application.web.resource.UsersResource"
        ));

        // Register JAX-RS provider classes
        deployment.setScannedProviderClasses(List.of(
            "org.example.realworldapi.infrastructure.web.security.JwtSecurityFilter",
            "org.example.realworldapi.infrastructure.web.security.RolesAllowedFilter",
            "org.example.realworldapi.infrastructure.web.provider.JacksonConfigProvider",
            "org.example.realworldapi.infrastructure.web.mapper.BeanValidationExceptionMapper",
            "org.example.realworldapi.infrastructure.web.mapper.BusinessExceptionMapper",
            "org.example.realworldapi.infrastructure.web.mapper.GenericExceptionMapper"
        ));

        // Build Undertow deployment
        BeanManager beanManager = container.getBeanManager();
        DeploymentInfo di = new DeploymentInfo()
                .addServletContextAttribute(
                    ResteasyDeployment.class.getName(), deployment)
                .addServletContextAttribute(
                    "jakarta.enterprise.inject.spi.BeanManager", beanManager)
                .setContextPath("/api")
                .setDeploymentName("ResteasyUndertow")
                .setClassLoader(Main.class.getClassLoader())
                .addListener(
                    Servlets.listener(
                        org.jboss.weld.environment.servlet.Listener.class))
                .addServlet(
                    Servlets.servlet("ResteasyServlet",
                        org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.class)
                        .setAsyncSupported(true)
                        .setLoadOnStartup(1)
                        .addMapping("/*")
                );

        UndertowJaxrsServer server = new UndertowJaxrsServer();
        server.start(
            Undertow.builder()
                .addHttpListener(port, host)
        );
        server.deploy(di);

        LOG.info("RealWorld API started on port {}", port);
        LOG.info("API available at http://{}:{}/api", host, port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down...");
            server.stop();
            container.close();
        }));
    }
}
