package jakarta.tutorial.taskcreator;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;

@Named("taskCreatorBean")
@ViewScoped
@Component
public class TaskCreatorBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ApplicationContext applicationContext;

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
            Task task = applicationContext.getBean(Task.class);
            task.init(taskName, taskType);
            taskService.submitTask(task, taskType);
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
