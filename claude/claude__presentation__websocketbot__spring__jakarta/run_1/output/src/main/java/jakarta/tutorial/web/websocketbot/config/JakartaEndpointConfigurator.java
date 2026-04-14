package jakarta.tutorial.web.websocketbot.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.websocket.server.ServerEndpointConfig;

@ApplicationScoped
public class JakartaEndpointConfigurator extends ServerEndpointConfig.Configurator {

  @Override
  public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
    return CDI.current().select(clazz).get();
  }
}
