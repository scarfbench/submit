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
package jakarta.tutorial.guessnumber;

import java.io.Serializable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.annotation.PostConstruct;

@Controller
@SessionAttributes({"userNumberBean"})
public class UserNumberBean implements Serializable {

    private static final long serialVersionUID = -7698506329160109476L;

    private int number;
    private Integer userNumber;
    private int minimum;
    private int remainingGuesses;

    @Autowired
    @MaxNumber
    private Integer maxNumber;

    private int maximum;

    @Autowired
    @Random
    private ObjectProvider<Integer> randomInt;

    private String message = "";

    public UserNumberBean() {
    }

    public int getNumber() {
        return number;
    }

    public void setUserNumber(Integer user_number) {
        userNumber = user_number;
    }

    public Integer getUserNumber() {
        return userNumber;
    }

    public int getMaximum() {
        return (this.maximum);
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public int getMinimum() {
        return (this.minimum);
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public int getRemainingGuesses() {
        return remainingGuesses;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @ModelAttribute("userNumberBean")
    public UserNumberBean getUserNumberBean() {
        return this;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (remainingGuesses == 0 && number != userNumber) {
            reset();
        }
        model.addAttribute("userNumberBean", this);
        return "index";
    }

    @PostMapping("/guess")
    public String check(@RequestParam("userNumber") Integer guessedNumber, Model model) {
        this.userNumber = guessedNumber;
        message = "";

        if (userNumber < minimum || userNumber > maximum) {
            message = "Invalid guess - must be between " + minimum + " and " + maximum;
        } else {
            if (userNumber > number) {
                maximum = userNumber - 1;
                message = "Lower!";
            } else if (userNumber < number) {
                minimum = userNumber + 1;
                message = "Higher!";
            } else {
                message = "Correct!";
            }
            remainingGuesses--;
        }

        model.addAttribute("userNumberBean", this);
        return "index";
    }

    @PostMapping("/reset")
    public String resetGame(Model model) {
        reset();
        model.addAttribute("userNumberBean", this);
        return "redirect:/";
    }

    @PostConstruct
    public void reset() {
        this.minimum = 0;
        this.userNumber = 0;
        this.remainingGuesses = 10;
        this.maximum = maxNumber;
        this.number = randomInt.getObject();
        this.message = "";
    }
}
