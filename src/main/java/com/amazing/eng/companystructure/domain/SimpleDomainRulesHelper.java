package com.amazing.eng.companystructure.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple business domain rules helper.
 */
public final class SimpleDomainRulesHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDomainRulesHelper.class);

    /**
     * Private constructor for utility class.
     */
    private SimpleDomainRulesHelper() {

    }

    /**
     * Returns true if <code>organizationalRelation</code> is not the coordinating organization unit (root)
     * for the whole company.
     *
     * @param organizationalRelation the relation to be tested
     * @return true if the relation is not the root, otherwise false
     */
    public static boolean isRoot(final OrganizationalRelation organizationalRelation) {
        return organizationalRelation == null || organizationalRelation.getReportsTo() == -1;
    }

    /**
     * Return true if a pair of Reports-to-Relation is present in the collection.
     *
     * @param organizationalRelations the relations collection to be tested
     * @return true if a pair of Reports-to-Relation is present, otherwise false
     */
    public static boolean isNotAReportsToRelationPair(final Collection<OrganizationalRelation> organizationalRelations) {
        return organizationalRelations.size() != 2;
    }

    /**
     * Returns true if the relation <code>ou</code> - Reports-to-Relation --> <code>reportsTo</code> is a cycle.
     *
     * @param ou        the coordinating (parent) organization unit
     * @param reportsTo the dependant(child) organization unit
     * @return true if relation pair is not a cycle, otherwise false
     */
    public static boolean isReportsToRelationACycle(final OrganizationalRelation ou,
                                                    final OrganizationalRelation reportsTo) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(ou.getPath()));
        Matcher matcher = pattern.matcher(reportsTo.getPath());
        boolean isAMatch = matcher.find();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Regex test [^%s, %s, %s]", Pattern.quote(ou.getPath()),
                    reportsTo.getPath(), isAMatch));
        }

        return isAMatch;
    }
}
