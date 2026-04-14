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
package jakarta.tutorial.async.ejb;

import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import io.smallrye.common.annotation.NonBlocking;

/**
 * Quarkus-based async mailer bean.
 * Uses CompletionStage instead of EJB @Asynchronous.
 *
 * @author ievans
 */
@Named
@ApplicationScoped
public class MailerBean {

    private static final Logger logger
            = Logger.getLogger(MailerBean.class.getName());

    @NonBlocking
    public CompletionStage<String> sendMessage(String email) {
        return CompletableFuture.supplyAsync(() -> {
            String status;
            try {
                Properties properties = new Properties();
                properties.put("mail.smtp.host", "localhost");
                properties.put("mail.smtp.port", "3025");
                properties.put("mail.smtp.auth", "false");
                properties.put("mail.smtp.starttls.enable", "false");

                Session session = Session.getInstance(properties);
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("jack@localhost"));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(email, false));
                message.setSubject("Test message from async example");
                message.setHeader("X-Mailer", "Jakarta Mail");
                DateFormat dateFormatter = DateFormat
                        .getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
                Date timeStamp = new Date();
                String messageBody = "This is a test message from the async "
                        + "example migrated to Quarkus. It was sent on "
                        + dateFormatter.format(timeStamp)
                        + ".";
                message.setText(messageBody);
                message.setSentDate(timeStamp);
                Transport.send(message);
                status = "Sent";
                logger.log(Level.INFO, "Mail sent to {0}", email);
            } catch (MessagingException ex) {
                logger.severe("Error in sending message.");
                status = "Encountered an error: " + ex.getMessage();
                logger.severe(ex.getMessage());
            }
            return status;
        });
    }
}
