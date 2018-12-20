package com.amazing.eng.companystructure.domain;

public final class OrganizationalRelation extends OrganizationUnitResponse {
    private String path;
    private String reportsToPath;

    public OrganizationalRelation(int organizationUnit, int reportsTo, int height, int root) {
        this.organizationUnit = organizationUnit;
        this.reportsTo = reportsTo;
        this.height = height;
        this.root = root;
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

    public OrganizationalRelation(int organizationUnit, int reportsTo, String path, String reportsToPath,
                                  int height, int root) {
        this(organizationUnit, reportsTo, path);
        this.reportsToPath = reportsToPath;
        this.organizationUnit = organizationUnit;
        this.height = height;
        this.root = root;
    }


    public String getPath() {
        return path;
    }

    public String getReportsToPath() {
        return reportsToPath;
    }
}
