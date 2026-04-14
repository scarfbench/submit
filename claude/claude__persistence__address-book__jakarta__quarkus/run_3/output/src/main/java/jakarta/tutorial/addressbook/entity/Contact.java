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
package jakarta.tutorial.addressbook.entity;

import java.util.Date;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

/**
 * Contact entity - migrated from Jakarta EE to Quarkus Panache.
 * PanacheEntity provides id field and basic CRUD operations.
 */
@Entity
public class Contact extends PanacheEntity {

    @NotNull
    public String firstName;

    @NotNull
    public String lastName;

    @Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
            + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9]"
            + "(?:[a-z0-9-]*[a-z0-9])?",
            message = "Not a valid email address.")
    public String email;

    @Pattern(regexp = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$",
            message = "Not a valid phone number.")
    public String mobilePhone;

    @Pattern(regexp = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$",
            message = "Not a valid phone number.")
    public String homePhone;

    @Temporal(TemporalType.DATE)
    @Past
    public Date birthday;

    public Contact() {
    }

    public Contact(String firstName, String lastName, String email,
                   String mobilePhone, String homePhone, Date birthday) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobilePhone = mobilePhone;
        this.homePhone = homePhone;
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Contact[id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }
}
