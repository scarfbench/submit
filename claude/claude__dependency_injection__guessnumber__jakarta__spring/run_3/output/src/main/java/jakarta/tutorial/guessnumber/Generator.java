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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Generator implements Serializable {

   private static final long serialVersionUID = -7213673465118041882L;

   private final java.util.Random random =
       new java.util.Random( System.currentTimeMillis() );

   private final int maxNumber = 100;

   java.util.Random getRandom() {
       return random;
   }

   @Bean
   @Random
   @Scope("prototype")
   public Integer randomNumber() {
       return getRandom().nextInt(maxNumber + 1);
   }

   @Bean
   @MaxNumber
   public Integer maxNumber() {
       return maxNumber;
   }

}
