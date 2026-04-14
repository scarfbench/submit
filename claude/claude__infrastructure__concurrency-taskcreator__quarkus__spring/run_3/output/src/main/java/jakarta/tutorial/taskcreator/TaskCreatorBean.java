package jakarta.tutorial.taskcreator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Set;

@Named("taskCreatorBean")
@Component
@Scope("session")
public class TaskCreatorBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    TaskService taskService;

    private String taskMessages = "";
    private String taskType = "IMMEDIATE";
    private String taskName = "";
    private String periodicTask = "";

    public String getTaskMessages() {
        return taskService.getInfoField();
    }

    public void setTaskMessages(String msg) {
        this.taskMessages = msg;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String t) {
        this.taskType = t;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String n) {
        this.taskName = n;
    }

    public String getPeriodicTask() {
        return periodicTask;
    }

    public void setPeriodicTask(String t) {
        this.periodicTask = t;
    }

    public Set<String> getPeriodicTasks() {
        return taskService.getPeriodicTasks();
    }

    public void submitTask() {
        if (!taskService.getPeriodicTasks().contains(taskName)) {
            taskService.submitTask(taskName, taskType);
            taskType = "IMMEDIATE";
            taskName = "";
        }
    }

    public void cancelTask() {
        taskService.cancelPeriodicTask(periodicTask);
    }

    public void clearInfoField() {
        taskService.clearInfoField();
    }
}
