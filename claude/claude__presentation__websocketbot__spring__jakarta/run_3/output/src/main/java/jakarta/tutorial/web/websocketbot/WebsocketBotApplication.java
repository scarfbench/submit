package jakarta.tutorial.web.websocketbot;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@WebListener
public class WebsocketBotApplication implements ServletContextListener {

  private static Executor websocketBotExecutor;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();
    ServerContainer serverContainer = (ServerContainer) servletContext
        .getAttribute(ServerContainer.class.getName());

    // Initialize the executor for async bot responses
    websocketBotExecutor = Executors.newCachedThreadPool();

    try {
      // Register the WebSocket endpoint
      ServerEndpointConfig config = ServerEndpointConfig.Builder
          .create(BotEndpoint.class, "/websocketbot")
          .build();
      serverContainer.addEndpoint(config);
    } catch (Exception e) {
      throw new RuntimeException("Failed to register WebSocket endpoint", e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Cleanup if needed
  }

  public static Executor getExecutor() {
    return websocketBotExecutor;
  }
}
