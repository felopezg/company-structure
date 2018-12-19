package com.amazing.eng.companystructure.domain;

import java.io.Serializable;

public final class OrganizationUnitRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int organizationUnit;
    private String name;
    private String description;

    /**
     * Public no-arg constructor required.
     */
    public OrganizationUnitRequest() {

    }

    public OrganizationUnitRequest(final int organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public OrganizationUnitRequest(final int organizationUnit, final String name, final String description) {
        this(organizationUnit);
        this.name = name;
        this.description = description;
    }

    public int getOrganizationUnit() {
        return organizationUnit;
    }

    public void setOrganizationUnit(int organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
