package jakarta.tutorial.web.websocketbot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

@ApplicationScoped
public class WebsocketBotApplication {

  @Produces
  @ApplicationScoped
  public Executor websocketBotExecutor() {
    return Executors.newCachedThreadPool();
  }

}
