package jakarta.tutorial.taskcreator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class TaskUpdateEvents {
    private final Event<String> event;

    @Inject
    public TaskUpdateEvents(Event<String> event) {
        this.event = event;
    }

    public void fire(String name) {
        event.fire(name);
    }
}