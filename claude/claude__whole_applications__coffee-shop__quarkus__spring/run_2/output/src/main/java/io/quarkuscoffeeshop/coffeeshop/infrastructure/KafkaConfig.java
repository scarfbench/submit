package io.quarkuscoffeeshop.coffeeshop.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaConfig {
    // This configuration class will only be active if spring.kafka.bootstrap-servers is set
}
