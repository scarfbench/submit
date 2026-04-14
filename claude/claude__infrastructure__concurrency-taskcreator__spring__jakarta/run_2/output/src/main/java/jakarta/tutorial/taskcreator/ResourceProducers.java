package jakarta.tutorial.taskcreator;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

@ApplicationScoped
public class ResourceProducers {

    @Produces
    @ApplicationScoped
    public Client createClient() {
        return ClientBuilder.newClient();
    }

    @Resource
    @Produces
    private ManagedExecutorService managedExecutorService;

    @Resource
    @Produces
    private ManagedScheduledExecutorService managedScheduledExecutorService;
}
