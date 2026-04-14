package org.eclipse.cargotracker.infrastructure.logging;

import java.util.logging.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class LoggerProducer {

    @Bean
    @Scope("prototype")
    public Logger produceLogger(InjectionPoint injectionPoint) {
        if (injectionPoint.getMethodParameter() != null) {
            return Logger.getLogger(injectionPoint.getMethodParameter().getContainingClass().getName());
        }
        if (injectionPoint.getField() != null) {
            return Logger.getLogger(injectionPoint.getField().getDeclaringClass().getName());
        }
        return Logger.getLogger("default");
    }
}
