package spring.tutorial.web.websocketbot;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

@QuarkusMain
public class WebsocketBotApplication implements QuarkusApplication {
  public static void main(String[] args) {
    Quarkus.run(WebsocketBotApplication.class, args);
  }

  @Override
  public int run(String... args) throws Exception {
    Quarkus.waitForExit();
    return 0;
  }

  @ApplicationScoped
  public static class Producers {
    @Produces
    @ApplicationScoped
    @WebsocketBotExecutor
    public Executor websocketBotExecutor() {
      return Executors.newCachedThreadPool();
    }
  }
}
