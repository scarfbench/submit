package jakarta.tutorial.web.dukeetf2;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

@Disabled("Integration test requires running server")
class WebSocketIT {

    private static final int DEFAULT_PORT = 8080;

    @Test
    void streamsFirstMessage() throws Exception {
        URI uri = new URI("ws://localhost:" + DEFAULT_PORT + "/dukeetf");
        CountDownLatch latch = new CountDownLatch(1);
        List<String> received = new CopyOnWriteArrayList<>();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session s = container.connectToServer(new Endpoint() {
            @Override public void onOpen(Session session, EndpointConfig config) {
                session.addMessageHandler(String.class, m -> { received.add(m); latch.countDown(); });
            }
        }, ClientEndpointConfig.Builder.create().build(), uri);

        try {
            boolean ok = latch.await(5, TimeUnit.SECONDS);
            assertThat(ok).isTrue();
            assertThat(received.get(0)).matches("\\d+\\.\\d{2} / -?\\d+");
        } finally {
            s.close();
        }
    }
}