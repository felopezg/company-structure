package com.amazing.eng.companystructure.utils;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

public class RandomOrganizationMaker {
    private static int SEQUENCE_ID = 0;

    /**
     * We are creating sample nodes based on height, so each organizational unit (vertex) gets enough
     * subdivisions (children).
     */
    private static int MAX_HEIGHT = 8;

    private static String ORGANIZATION_UNITS_DROP = "DROP TABLE organization_units;%n";

    private static String ORGANIZATION_UNITS_CREATE = "CREATE TABLE organization_units("
            + "  id INT PRIMARY KEY, "
            + "  name VARCHAR(50), "
            + "  description VARCHAR(250));%n";

    private static String ORGANIZATIONAL_STRUCTURE_DROP = "DROP TABLE organizational_structure;%n";

    /**
     * We are going to use a organization unit enumeration (edge enumeration) approach.
     */
    private static String ORGANIZATIONAL_STRUCTURE_CREATE = "CREATE TABLE organizational_structure("
            + "organization_unit INT NOT NULL, "
            + "reports_to INT NOT NULL, "
            + "path VARCHAR(60000) NOT NULL);%n";

    private static String ORGANIZATIONAL_STRUCTURE_INDEXES =
            "CREATE INDEX idx_ous_path ON organizational_structure(path);%n"
                    + "CREATE INDEX idx_ous_organization_unit ON organizational_structure(organization_unit);%n"
                    + "CREATE INDEX idx_ous_reports_to ON organizational_structure(reports_to);%n";


    private static String ORGANIZATIONAL_STRUCTURE_INSERT = "INSERT INTO organizational_structure " +
            "(organization_unit, reports_to, path) VALUES (%d, %d,'%s');%n";

    private static String ORGANIZATION_UNITS_INSERT = "INSERT INTO organization_units " +
            "(id, name, description) VALUES (%d, '%s','%s');%n";

    private static String ORGANIZATION_UNITS_ID_SEQUENCE_DROP = "DROP SEQUENCE ou_seq;%n ";

    private static String ORGANIZATION_UNITS_ID_SEQUENCE_CREATE = "CREATE SEQUENCE ou_seq START WITH %d;%n ";

    public static void main(String[] args) throws IOException {
        final Random rand = new Random();
        final OrganizationUnit root = new OrganizationUnit();
        root.path.add(root.id);

        final File file = new File("OU_SAMPLE.sql");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);


        writer.write(String.format(ORGANIZATION_UNITS_DROP));
        writer.write(String.format(ORGANIZATION_UNITS_CREATE));

        writer.write(String.format(ORGANIZATIONAL_STRUCTURE_DROP));
        writer.write(String.format(ORGANIZATIONAL_STRUCTURE_CREATE));
        writer.write(String.format(ORGANIZATIONAL_STRUCTURE_INDEXES));


        Stack<OrganizationUnit> stack = new Stack<>();
        stack.push(root);

        int subDivisionsForLevel;
        OrganizationUnit parentOu, ou;
        while (!stack.empty()) {

            //Let's pop the parent node to iterate over its children
            parentOu = stack.pop();

            //Let's write/insert them in pre-order in the file/db.
            writer.write(String.format(ORGANIZATION_UNITS_INSERT,
                    parentOu.id,
                    String.valueOf(parentOu.id),
                    String.valueOf(parentOu.id)));

            writer.write(String.format(ORGANIZATIONAL_STRUCTURE_INSERT,
                    parentOu.id,
                    parentOu.reports_to,
                    parentOu.path.stream().map(Object::toString).collect(Collectors.joining(".", "", "."))));

            subDivisionsForLevel = parentOu.path.size() < MAX_HEIGHT ? rand.nextInt(8) + 1 : 0;

            for (int i = 1; i < subDivisionsForLevel; i++) {
                ou = new OrganizationUnit(parentOu.id);
                ou.path.addAll(parentOu.path);
                ou.path.add(ou.id);
                stack.push(ou);
            }
        }

        writer.write(String.format(ORGANIZATION_UNITS_ID_SEQUENCE_CREATE, SEQUENCE_ID + 1));

        writer.flush();
        writer.close();

    }

    static class OrganizationUnit {
        int id;
        int reports_to;
        List<Integer> path;

        OrganizationUnit() {
            this.id = ++SEQUENCE_ID;
            this.reports_to = -1;
            this.path = new ArrayList<>();
        }

        OrganizationUnit(int reports_to) {
            this.id = ++SEQUENCE_ID;
            this.reports_to = reports_to;
            this.path = new ArrayList<>();
        }
    }


}
