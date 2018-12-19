package com.amazing.eng.companystructure.domain;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleDomainRulesHelperTest {

    @Test
    public void isRelationPairNotACycle() {


    }


    @Test
    public void isReportsToRelationACycleWithSimplePathsAndNotACycle() {
        OrganizationalRelation ou = new OrganizationalRelation(1, -1, "1.2.3"  );
        OrganizationalRelation reportsTo =  new OrganizationalRelation(3,  2, "1.2.4.3.9");

        Assert.assertEquals( false,
                SimpleDomainRulesHelper.isReportsToRelationACycle(ou, reportsTo));
    }

    @Test
    public void isReportsToRelationACycleWithOuPromotionAndNotACycle() {
        OrganizationalRelation ou = new OrganizationalRelation(1, -1, "1.2.3.4.3.9"  );
        OrganizationalRelation reportsTo =  new OrganizationalRelation(3,  2, "1.2.4");

        Assert.assertEquals(false,
                SimpleDomainRulesHelper.isReportsToRelationACycle(ou, reportsTo));
    }

    @Test
    public void isReportsToRelationACycleWithMatchingSubPathsAndNotACycle() {
        OrganizationalRelation ou = new OrganizationalRelation(1, -1, "1.3."  );
        OrganizationalRelation reportsTo =  new OrganizationalRelation(3,  2, "1.2.11.3.9");

        Assert.assertEquals(false,
                SimpleDomainRulesHelper.isReportsToRelationACycle(ou, reportsTo));
    }

    @Test
    public void isReportsToRelationACycleWithSimplePathAndACycle() {
        OrganizationalRelation ou = new OrganizationalRelation(1, -1, "1."  );
        OrganizationalRelation reportsTo =  new OrganizationalRelation(3,  2, "1.2.3."  );

        Assert.assertEquals(true,
                SimpleDomainRulesHelper.isReportsToRelationACycle(ou, reportsTo));
    }

    @Test
    public void isReportsToRelationACycleWithPseudoMatchingSubPathsAndACycle() {
        OrganizationalRelation ou = new OrganizationalRelation(1, -1, "1."  );
        OrganizationalRelation reportsTo =  new OrganizationalRelation(3,  2, "1.11.");

        Assert.assertEquals(true,
                SimpleDomainRulesHelper.isReportsToRelationACycle(ou, reportsTo));
    }

}