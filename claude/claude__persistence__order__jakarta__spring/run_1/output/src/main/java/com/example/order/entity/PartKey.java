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
package com.example.order.entity;

import java.io.Serializable;
import java.util.Objects;

public class PartKey implements Serializable {
    private static final long serialVersionUID = -3162267592969127613L;
    private String partNumber;
    private int revision;

    public PartKey() {}

    public PartKey(String partNumber, int revision) {
        this.partNumber = partNumber;
        this.revision = revision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(partNumber, revision);
    }

    @Override
    public boolean equals(Object otherOb) {
        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof PartKey other)) {
            return false;
        }
        return Objects.equals(this.partNumber, other.partNumber)
                && this.revision == other.revision;
    }

    @Override
    public String toString() {
        return partNumber + " rev" + revision;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }
}
