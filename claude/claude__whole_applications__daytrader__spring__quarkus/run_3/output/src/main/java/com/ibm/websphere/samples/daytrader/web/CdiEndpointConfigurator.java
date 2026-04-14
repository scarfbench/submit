package com.ibm.websphere.samples.daytrader.web;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.websocket.server.ServerEndpointConfig;

public class CdiEndpointConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return CDI.current().select(endpointClass).get();
    }
}
