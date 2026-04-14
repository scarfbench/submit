package jakarta.tutorial.web.websocketbot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

@ApplicationPath("/api")
@ApplicationScoped
public class WebsocketBotApplication extends Application {

  @Produces
  @ApplicationScoped
  public Executor websocketBotExecutor() {
    return Executors.newCachedThreadPool();
  }
}
