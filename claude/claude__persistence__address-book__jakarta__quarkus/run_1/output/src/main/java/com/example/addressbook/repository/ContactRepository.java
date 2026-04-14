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
package com.example.addressbook.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import com.example.addressbook.entity.Contact;

/**
 * Repository for Contact entity - replaces the EJB ContactFacade.
 * Uses Quarkus Panache repository pattern instead of EJB Stateless beans.
 */
@ApplicationScoped
public class ContactRepository implements PanacheRepository<Contact> {
    // PanacheRepository provides all CRUD operations:
    // persist(), delete(), findById(), listAll(), count(), etc.
}
