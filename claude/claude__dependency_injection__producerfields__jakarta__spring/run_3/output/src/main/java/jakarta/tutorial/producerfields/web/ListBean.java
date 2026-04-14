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

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.tutorial.producerfields.ejb.RequestBean;
import jakarta.tutorial.producerfields.entity.ToDo;
import jakarta.validation.constraints.NotNull;

@Controller
@SessionScope
public class ListBean implements Serializable {

    private static final long serialVersionUID = 8751711591138727525L;

    @Autowired
    private RequestBean request;

    @NotNull
    private String inputString;
    private ToDo toDo;
    private List<ToDo> toDos;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("toDos", request.getToDos());
        return "index";
    }

    @PostMapping("/createTask")
    public String createTask(@RequestParam("inputString") String inputString, Model model) {
        this.inputString = inputString;
        this.toDo = request.createToDo(inputString);
        model.addAttribute("toDos", request.getToDos());
        model.addAttribute("message", "Task created successfully!");
        return "index";
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public ToDo getToDo() {
        return toDo;
    }

    public void setToDo(ToDo toDo) {
        this.toDo = toDo;
    }

    public List<ToDo> getToDos() {
        return request.getToDos();
    }

    public void setToDos(List<ToDo> toDos) {
        this.toDos = toDos;
    }
}
