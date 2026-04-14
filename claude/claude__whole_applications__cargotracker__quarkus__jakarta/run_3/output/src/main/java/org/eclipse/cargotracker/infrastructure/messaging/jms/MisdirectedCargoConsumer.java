package org.eclipse.cargotracker.infrastructure.messaging.jms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

@Singleton
@Startup
public class MisdirectedCargoConsumer implements Runnable {

  @Inject
  private Logger logger;

  @Inject
  private ConnectionFactory connectionFactory;

  @Inject
  @ConfigProperty(name = "app.jms.MisdirectedCargoQueue", defaultValue = "MisdirectedCargoQueue")
  private String misdirectedCargoQueue;

  private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

  @PostConstruct
  public void onStart() {
    scheduler.submit(this);
  }

  @PreDestroy
  public void onStop() {
    scheduler.shutdown();
  }

  @Override
  public void run() {
    try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
      JMSConsumer consumer = context.createConsumer(context.createQueue(misdirectedCargoQueue));
      while (true) {
        Message message = consumer.receive();
        logger.log(
            Level.INFO, "Cargo with tracking ID {0} misdirected.", message.getBody(String.class));
      }
    } catch (JMSException ex) {
      logger.log(Level.WARNING, "Error processing message.", ex);
    }
  }

}
