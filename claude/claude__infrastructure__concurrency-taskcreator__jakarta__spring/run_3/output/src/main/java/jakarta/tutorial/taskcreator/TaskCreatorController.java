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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TaskCreatorController {

    @Autowired
    private TaskCreatorBean taskCreatorBean;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("bean", taskCreatorBean);
        return "index";
    }

    @PostMapping("/submitTask")
    @ResponseBody
    public String submitTask(@RequestParam String taskType, @RequestParam String taskName) {
        taskCreatorBean.setTaskType(taskType);
        taskCreatorBean.setTaskName(taskName);
        taskCreatorBean.submitTask();
        return "success";
    }

    @PostMapping("/cancelTask")
    @ResponseBody
    public String cancelTask(@RequestParam String periodicTask) {
        taskCreatorBean.setPeriodicTask(periodicTask);
        taskCreatorBean.cancelTask();
        return "success";
    }

    @PostMapping("/clearLog")
    @ResponseBody
    public String clearLog() {
        taskCreatorBean.clearInfoField();
        return "success";
    }

    @GetMapping("/taskMessages")
    @ResponseBody
    public String getTaskMessages() {
        return taskCreatorBean.getTaskMessages();
    }

    @GetMapping("/periodicTasks")
    @ResponseBody
    public java.util.Set<String> getPeriodicTasks() {
        return taskCreatorBean.getPeriodicTasks();
    }
}
