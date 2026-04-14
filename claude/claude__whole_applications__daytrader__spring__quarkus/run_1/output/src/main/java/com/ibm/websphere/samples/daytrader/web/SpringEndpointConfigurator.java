package com.ibm.websphere.samples.daytrader.web;

import jakarta.websocket.server.ServerEndpointConfig;
import jakarta.enterprise.inject.spi.CDI;

public class SpringEndpointConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        try {
            return CDI.current().select(endpointClass).get();
        } catch (Exception e) {
            try {
                return endpointClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new InstantiationException(ex.getMessage());
            }
        }
    }
}
