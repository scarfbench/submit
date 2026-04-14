package jakarta.tutorial.taskcreator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Helper to decouple event publishing. */
@Component
public class TaskUpdateEvents {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void fire(String name) {
        eventPublisher.publishEvent(name);
    }
}
