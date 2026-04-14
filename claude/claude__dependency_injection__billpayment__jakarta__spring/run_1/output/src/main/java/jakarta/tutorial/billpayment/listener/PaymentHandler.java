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
package jakarta.tutorial.billpayment.listener;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.tutorial.billpayment.interceptor.Logged;
import jakarta.tutorial.billpayment.payment.CreditPaymentEvent;
import jakarta.tutorial.billpayment.payment.DebitPaymentEvent;

/**
 * Handler for the two kinds of PaymentEvent.
 */
@Logged
@Component
@SessionScope
public class PaymentHandler implements Serializable {

    private static final Logger logger =
            Logger.getLogger(PaymentHandler.class.getCanonicalName());
    private static final long serialVersionUID = 2013564481486393525L;

    public PaymentHandler() {
        logger.log(Level.INFO, "PaymentHandler created.");
    }

    @EventListener
    public void creditPayment(CreditPaymentEvent event) {
        logger.log(Level.INFO, "PaymentHandler - Credit Handler: {0}",
                event.getPaymentEvent().toString());

        // call a specific Credit handler class...
    }

    @EventListener
    public void debitPayment(DebitPaymentEvent event) {
        logger.log(Level.INFO, "PaymentHandler - Debit Handler: {0}",
                event.getPaymentEvent().toString());

        // call a specific Debit handler class...
    }
}
