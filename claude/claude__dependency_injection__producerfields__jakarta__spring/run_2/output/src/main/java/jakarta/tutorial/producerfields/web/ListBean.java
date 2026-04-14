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

import java.util.List;

import jakarta.tutorial.producerfields.ejb.RequestBean;
import jakarta.tutorial.producerfields.entity.ToDo;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ListBean {

    @Autowired
    private RequestBean requestBean;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        List<ToDo> todos = requestBean.getToDos();
        model.addAttribute("todos", todos);
        return "index";
    }

    @PostMapping("/createTask")
    public String createTask(@RequestParam("inputString") @NotNull String inputString, Model model) {
        ToDo newTodo = requestBean.createToDo(inputString);
        model.addAttribute("todo", newTodo);
        return "redirect:/todolist";
    }

    @GetMapping("/todolist")
    public String todoList(Model model) {
        List<ToDo> todos = requestBean.getToDos();
        model.addAttribute("todos", todos);
        return "todolist";
    }
}
