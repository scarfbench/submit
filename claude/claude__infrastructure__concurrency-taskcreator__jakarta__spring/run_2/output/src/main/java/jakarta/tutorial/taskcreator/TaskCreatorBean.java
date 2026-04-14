/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.taskcreator;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/* Controller for the web interface */
@Controller
public class TaskCreatorBean {

    private static final Logger log = Logger.getLogger("TaskCreatorBean");

    @Autowired
    private TaskService taskService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("taskMessages", taskService.getInfoField());
        model.addAttribute("taskType", "IMMEDIATE");
        model.addAttribute("taskName", "");
        model.addAttribute("periodicTask", "");
        model.addAttribute("periodicTasks", taskService.getPeriodicTasks());
        return "index";
    }

    @PostMapping("/submitTask")
    @ResponseBody
    public String submitTask(@RequestParam String taskName, @RequestParam String taskType) {
        if (!taskService.getPeriodicTasks().contains(taskName)) {
            /* Create a new task object */
            Task task = new Task(taskName, taskType);
            /* Use the managed executor objects to run the task */
            taskService.submitTask(task, taskType);
        }
        return "success";
    }

    @PostMapping("/cancelTask")
    @ResponseBody
    public String cancelTask(@RequestParam String periodicTask) {
        log.log(Level.INFO, "[TaskCreatorBean] Cancelling task {0}", periodicTask);
        taskService.cancelPeriodicTask(periodicTask);
        return "success";
    }

    @PostMapping("/clearLog")
    @ResponseBody
    public String clearInfoField() {
        taskService.clearInfoField();
        return "success";
    }

    @GetMapping("/getMessages")
    @ResponseBody
    public String getTaskMessages() {
        return taskService.getInfoField();
    }

    @GetMapping("/getPeriodicTasks")
    @ResponseBody
    public Set<String> getPeriodicTasks() {
        return taskService.getPeriodicTasks();
    }
}
