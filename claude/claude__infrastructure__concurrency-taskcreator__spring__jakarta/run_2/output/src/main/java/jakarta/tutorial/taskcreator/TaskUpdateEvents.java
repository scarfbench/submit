package jakarta.tutorial.taskcreator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class TaskUpdateEvents {
    @Inject
    private Event<String> eventPublisher;

    public void fire(String name) {
        eventPublisher.fire(name);
    }
}