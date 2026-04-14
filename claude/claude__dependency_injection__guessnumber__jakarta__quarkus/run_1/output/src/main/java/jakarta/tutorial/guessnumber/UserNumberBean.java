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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserNumberBean implements Serializable {

    private static final long serialVersionUID = -7698506329160109476L;

    private int number;
    private Integer userNumber;
    private int minimum;
    private int remainingGuesses;
    @Inject
    @MaxNumber
    private int maxNumber;
    private int maximum;
    @Inject
    @Random
    Instance<Integer> randomInt;
    private String message;

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

    public String check(int guess) {
        this.userNumber = guess;
        message = null;

        if (userNumber > number) {
            maximum = userNumber - 1;
            message = "Lower!";
        }
        if (userNumber < number) {
            minimum = userNumber + 1;
            message = "Higher!";
        }
        if (userNumber == number) {
            message = "Correct!";
        }
        remainingGuesses--;
        return message;
    }

    @PostConstruct
    public void reset() {
        this.minimum = 0;
        this.userNumber = 0;
        this.remainingGuesses = 10;
        this.maximum = maxNumber;
        this.number = randomInt.get();
        this.message = null;
    }

    public boolean validateNumberRange(int input) {
        return input >= minimum && input <= maximum;
    }
}
