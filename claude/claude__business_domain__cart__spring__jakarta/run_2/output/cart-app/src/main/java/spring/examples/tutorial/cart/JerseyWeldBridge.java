package spring.examples.tutorial.cart;

import jakarta.inject.Inject;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jboss.weld.environment.se.WeldContainer;

public class JerseyWeldBridge extends AbstractBinder {

    private final WeldContainer weldContainer;

    public JerseyWeldBridge(WeldContainer weldContainer) {
        this.weldContainer = weldContainer;
    }

    @Override
    protected void configure() {
        bind(weldContainer).to(WeldContainer.class);
    }
}
