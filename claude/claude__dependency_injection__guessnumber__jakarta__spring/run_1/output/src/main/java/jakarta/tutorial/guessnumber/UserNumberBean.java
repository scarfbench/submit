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
import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.PostConstruct;

@Controller
@SessionScope
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
    private ObjectProvider<Integer> randomIntProvider;

    private String message = "";
    private String hint = "";

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

    public String getHint() {
        return hint;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("minimum", minimum);
        model.addAttribute("maximum", maximum);
        model.addAttribute("remainingGuesses", remainingGuesses);
        model.addAttribute("userNumber", userNumber);
        model.addAttribute("message", message);
        model.addAttribute("hint", hint);
        model.addAttribute("gameOver", userNumber != null && (userNumber == number || remainingGuesses <= 0));
        model.addAttribute("won", userNumber != null && userNumber == number);
        return "index";
    }

    @PostMapping("/guess")
    public String check(@RequestParam("userNumber") Integer guess, Model model) {
        message = "";
        hint = "";

        if (guess < minimum || guess > maximum) {
            message = "Invalid guess - must be between " + minimum + " and " + maximum;
        } else {
            userNumber = guess;

            if (userNumber > number) {
                maximum = userNumber - 1;
                hint = "Lower!";
            } else if (userNumber < number) {
                minimum = userNumber + 1;
                hint = "Higher!";
            } else {
                message = "Correct!";
            }
            remainingGuesses--;
        }

        model.addAttribute("minimum", minimum);
        model.addAttribute("maximum", maximum);
        model.addAttribute("remainingGuesses", remainingGuesses);
        model.addAttribute("userNumber", userNumber);
        model.addAttribute("message", message);
        model.addAttribute("hint", hint);
        model.addAttribute("gameOver", userNumber != null && (userNumber == number || remainingGuesses <= 0));
        model.addAttribute("won", userNumber != null && userNumber == number);

        return "index";
    }

    @PostMapping("/reset")
    public String reset(Model model) {
        this.minimum = 0;
        this.userNumber = 0;
        this.remainingGuesses = 10;
        this.maximum = maxNumber;
        this.number = randomIntProvider.getObject();
        this.message = "";
        this.hint = "";

        model.addAttribute("minimum", minimum);
        model.addAttribute("maximum", maximum);
        model.addAttribute("remainingGuesses", remainingGuesses);
        model.addAttribute("userNumber", userNumber);
        model.addAttribute("message", message);
        model.addAttribute("hint", hint);
        model.addAttribute("gameOver", false);
        model.addAttribute("won", false);

        return "index";
    }

    @PostConstruct
    public void init() {
        this.minimum = 0;
        this.userNumber = 0;
        this.remainingGuesses = 10;
        this.maximum = maxNumber;
        this.number = randomIntProvider.getObject();
    }
}
