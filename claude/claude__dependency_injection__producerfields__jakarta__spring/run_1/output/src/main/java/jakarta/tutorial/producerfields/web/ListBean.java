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
package jakarta.tutorial.producerfields.web;

import jakarta.tutorial.producerfields.ejb.RequestBean;
import jakarta.tutorial.producerfields.entity.ToDo;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

@Controller
public class ListBean implements Serializable {

    private static final long serialVersionUID = 8751711591138727525L;

    @Autowired
    private RequestBean request;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/createTask")
    public String createTask(@RequestParam("inputString") @NotNull String inputString, Model model) {
        if (inputString != null && !inputString.trim().isEmpty()) {
            ToDo toDo = request.createToDo(inputString);
            model.addAttribute("toDo", toDo);
            model.addAttribute("message", "Task created successfully!");
        }
        return "index";
    }

    @GetMapping("/todolist")
    public String showTodoList(Model model) {
        List<ToDo> toDos = request.getToDos();
        model.addAttribute("toDos", toDos);
        return "todolist";
    }
}
