package org.example.realworldapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class RestApplication extends Application {
    // Let RESTEasy-CDI discover @Path resources and @Provider classes automatically
    // Do NOT override getClasses() - that would cause POJO instantiation instead of CDI
}
