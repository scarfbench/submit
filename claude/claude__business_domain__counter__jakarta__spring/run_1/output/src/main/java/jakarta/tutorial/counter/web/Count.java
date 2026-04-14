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
package jakarta.tutorial.counter.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.tutorial.counter.ejb.CounterBean;

/**
 *
 * @author ian
 */
@Controller
public class Count {
    private final CounterBean counterBean;

    @Autowired
    public Count(CounterBean counterBean) {
        this.counterBean = counterBean;
    }

    @GetMapping("/")
    public String index(Model model) {
        int hitCount = counterBean.getHits();
        model.addAttribute("hitCount", hitCount);
        return "index";
    }
}
