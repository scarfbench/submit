package jakarta.tutorial.taskcreator;

public class TaskUpdateEvent {
    private final String message;

    public TaskUpdateEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
