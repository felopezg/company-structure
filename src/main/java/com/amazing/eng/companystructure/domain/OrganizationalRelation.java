package com.amazing.eng.companystructure.domain;

import java.io.Serializable;

public final class OrganizationalRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    private int organizationUnit;
    private int reportsTo;
    private String path;
    private String reportsToPath;

    /**
     * Public no-arg constructor required.
     */
    public OrganizationalRelation() {

    }

    public OrganizationalRelation(int organizationUnit, int reportsTo, String path) {
        this.organizationUnit = organizationUnit;
        this.path = path;
        this.reportsTo = reportsTo;
    }

    public OrganizationalRelation(int organizationUnit, int reportsTo, String path, String reportsToPath) {
        this(organizationUnit, reportsTo, path);
        this.reportsToPath = reportsToPath;
        this.organizationUnit = organizationUnit;
    }

    public int getOrganizationUnit() {
        return organizationUnit;
    }

    public String getPath() {
        return path;
    }

    public int getReportsTo() {
        return reportsTo;
    }

    public String getReportsToPath() {
        return reportsToPath;
    }
}
