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

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/game")
public class GameResource {

    @Inject
    UserNumberBean userNumberBean;

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public GameStatus getStatus() {
        return new GameStatus(
            userNumberBean.getMinimum(),
            userNumberBean.getMaximum(),
            userNumberBean.getRemainingGuesses(),
            userNumberBean.getMessage()
        );
    }

    @POST
    @Path("/guess")
    @Produces(MediaType.APPLICATION_JSON)
    public GameResponse guess(@QueryParam("number") int number) {
        if (!userNumberBean.validateNumberRange(number)) {
            return new GameResponse("Invalid guess - number must be between "
                + userNumberBean.getMinimum() + " and " + userNumberBean.getMaximum(), false);
        }

        String message = userNumberBean.check(number);
        boolean correct = "Correct!".equals(message);

        return new GameResponse(message, correct);
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public GameStatus reset() {
        userNumberBean.reset();
        return getStatus();
    }

    public static class GameStatus {
        public int minimum;
        public int maximum;
        public int remainingGuesses;
        public String message;

        public GameStatus() {}

        public GameStatus(int minimum, int maximum, int remainingGuesses, String message) {
            this.minimum = minimum;
            this.maximum = maximum;
            this.remainingGuesses = remainingGuesses;
            this.message = message;
        }
    }

    public static class GameResponse {
        public String message;
        public boolean correct;

        public GameResponse() {}

        public GameResponse(String message, boolean correct) {
            this.message = message;
            this.correct = correct;
        }
    }
}
