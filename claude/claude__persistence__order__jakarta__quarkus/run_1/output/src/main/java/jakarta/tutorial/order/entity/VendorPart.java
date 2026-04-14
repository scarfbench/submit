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
package jakarta.tutorial.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="PERSISTENCE_ORDER_VENDOR_PART",
       uniqueConstraints=
           @UniqueConstraint(columnNames={"PARTNUMBER", "PARTREVISION"})
)
@NamedQueries({
    @NamedQuery(
        name="findAverageVendorPartPrice",
        query="SELECT AVG(vp.price) " +
              "FROM VendorPart vp"
    ),
    @NamedQuery(
        name="findTotalVendorPartPricePerVendor",
        query="SELECT SUM(vp.price) " +
              "FROM VendorPart vp " +
              "WHERE vp.vendor.vendorId = :id"
    ),
    @NamedQuery(
        name="findAllVendorParts",
        query="SELECT vp FROM VendorPart vp ORDER BY vp.vendorPartNumber"
    )
})
public class VendorPart implements java.io.Serializable {
    private static final long serialVersionUID = 4685631589912848921L;

    @Id
    @SequenceGenerator(name = "vendorPartSeq", sequenceName = "VENDOR_PART_SEQ", allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vendorPartSeq")
    private Long vendorPartNumber;

    private String description;
    private double price;

    @OneToOne
    @JoinColumns({
        @JoinColumn(name="PARTNUMBER", referencedColumnName="PARTNUMBER"),
        @JoinColumn(name="PARTREVISION", referencedColumnName="REVISION")
    })
    @JsonIgnore
    private Part part;

    @JoinColumn(name="VENDORID")
    @ManyToOne
    @JsonIgnore
    private Vendor vendor;

    public VendorPart() {}

    public VendorPart(String description, double price, Part part) {
        this.description = description;
        this.price = price;
        this.part = part;
        part.setVendorPart(this);
    }

    public Long getVendorPartNumber() {
        return vendorPartNumber;
    }

    public void setVendorPartNumber(Long vendorPartNumber) {
        this.vendorPartNumber = vendorPartNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

}
