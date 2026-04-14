package org.example.realworldapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class RestApplication extends Application {
  // Resources and providers are registered via ResteasyDeployment in Main.java
}
