/**
 * CDI producer for Map<String, TradeServices>.
 *
 * In Spring, @Autowired Map<String, SomeInterface> auto-populates with all
 * beans of that type keyed by bean name. CDI does not support this pattern
 * natively, so this producer builds the map from all @Named TradeServices
 * implementations.
 */
package com.ibm.websphere.samples.daytrader.config;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
public class TradeServicesProducer {

    @Inject
    @Any
    private Instance<TradeServices> tradeServicesInstances;

    @Produces
    @ApplicationScoped
    public Map<String, TradeServices> produceTradeServicesMap() {
        Map<String, TradeServices> map = new HashMap<>();
        for (Instance.Handle<TradeServices> handle : tradeServicesInstances.handles()) {
            TradeServices ts = handle.get();
            // Check for @Named qualifier on the bean metadata
            for (Annotation qualifier : handle.getBean().getQualifiers()) {
                if (qualifier instanceof Named) {
                    String name = ((Named) qualifier).value();
                    if (name != null && !name.isEmpty()) {
                        map.put(name, ts);
                    }
                    break;
                }
            }
        }
        return map;
    }
}
