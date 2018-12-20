package com.amazing.eng.companystructure.domain;

import java.io.Serializable;

public class OrganizationUnitResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int organizationUnit;
    protected int reportsTo;
    protected int height;
    protected int root;

    /**
     * Public no-arg constructor required.
     */
    public OrganizationUnitResponse() {

    }

    public OrganizationUnitResponse(int organizationUnit, int reportsTo, int height, int root) {
        this.organizationUnit = organizationUnit;
        this.reportsTo = reportsTo;
        this.height = height;
        this.root = root;
    }

    public int getOrganizationUnit() {
        return organizationUnit;
    }

    public void setOrganizationUnit(int organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public int getReportsTo() {
        return reportsTo;
    }

    public void setReportsTo(int reportsTo) {
        this.reportsTo = reportsTo;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
    }
}
