package jakarta.tutorial.taskcreator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class TaskUpdateEvents {
    @Inject
    Event<String> event;

    public void fire(String name) {
        event.fire(name);
    }
}