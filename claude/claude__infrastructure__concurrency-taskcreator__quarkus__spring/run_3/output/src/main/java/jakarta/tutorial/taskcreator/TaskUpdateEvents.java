package jakarta.tutorial.taskcreator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Helper to decouple direct injection of event publisher. */
@Component
public class TaskUpdateEvents {
    @Autowired
    ApplicationEventPublisher eventPublisher;

    public void fire(String name) {
        eventPublisher.publishEvent(name);
    }
}
