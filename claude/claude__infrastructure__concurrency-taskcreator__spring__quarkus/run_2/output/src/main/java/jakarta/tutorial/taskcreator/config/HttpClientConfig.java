package jakarta.tutorial.taskcreator.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

@ApplicationScoped
public class HttpClientConfig {
    @Produces
    @ApplicationScoped
    public Client restClient() {
        return ClientBuilder.newClient();
    }
}