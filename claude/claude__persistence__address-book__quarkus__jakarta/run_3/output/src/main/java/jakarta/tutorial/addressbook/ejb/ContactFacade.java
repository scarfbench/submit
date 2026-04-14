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
package jakarta.tutorial.addressbook.ejb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.tutorial.addressbook.entity.Contact;
import jakarta.ejb.Stateless;

/**
 *
 * @author ian
 */
@Stateless
public class ContactFacade extends AbstractFacade<Contact> {

    @PersistenceContext(unitName = "addressbookPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ContactFacade() {
        super(Contact.class);
    }
}
