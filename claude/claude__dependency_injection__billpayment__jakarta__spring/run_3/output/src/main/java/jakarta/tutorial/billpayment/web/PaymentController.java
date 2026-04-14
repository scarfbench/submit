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
package jakarta.tutorial.billpayment.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.tutorial.billpayment.payment.PaymentBean;
import jakarta.validation.Valid;

/**
 * Spring MVC Controller for payment operations.
 */
@Controller
public class PaymentController {

    @Autowired
    private PaymentBean paymentBean;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("paymentBean", paymentBean);
        return "index";
    }

    @PostMapping("/pay")
    public String pay(@Valid @ModelAttribute PaymentBean paymentBean,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("paymentBean", this.paymentBean);
            return "index";
        }

        this.paymentBean.setValue(paymentBean.getValue());
        this.paymentBean.setPaymentOption(paymentBean.getPaymentOption());

        String resultPage = this.paymentBean.pay();
        model.addAttribute("paymentBean", this.paymentBean);
        return resultPage;
    }

    @PostMapping("/reset")
    public String reset(Model model) {
        paymentBean.reset();
        model.addAttribute("paymentBean", paymentBean);
        return "redirect:/";
    }
}
