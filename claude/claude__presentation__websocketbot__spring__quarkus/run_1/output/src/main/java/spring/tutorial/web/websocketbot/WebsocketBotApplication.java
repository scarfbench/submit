package spring.tutorial.web.websocketbot;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

@QuarkusMain
public class WebsocketBotApplication {
  public static void main(String[] args) {
    Quarkus.run(args);
  }

  @ApplicationScoped
  public static class ExecutorProducer {
    @Produces
    @ApplicationScoped
    @Named("websocketBotExecutor")
    public Executor websocketBotExecutor() {
      return Executors.newCachedThreadPool();
    }
  }
}
