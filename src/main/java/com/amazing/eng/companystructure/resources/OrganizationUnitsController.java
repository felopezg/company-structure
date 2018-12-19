package com.amazing.eng.companystructure.resources;

import com.amazing.eng.companystructure.database.OrganizationalStructureRepository;
import com.amazing.eng.companystructure.domain.OrganizationUnitRequest;
import com.amazing.eng.companystructure.domain.OrganizationUnitResponse;
import com.amazing.eng.companystructure.domain.OrganizationalRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.amazing.eng.companystructure.domain.SimpleDomainRulesHelper.*;

@RestController
@RequestMapping("/v1.0.0/organization-units")
public final class OrganizationUnitsController {
    private static final ResponseEntity<List<OrganizationUnitResponse>> OK =
            new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

    private static final ResponseEntity<List<OrganizationUnitResponse>> NOT_FOUND =
            new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);

    private static final ResponseEntity<List<OrganizationUnitResponse>> BAD_REQUEST =
            new ResponseEntity<>(Collections.emptyList(), HttpStatus.BAD_REQUEST);

    @Autowired
    private OrganizationalStructureRepository ouRepository;

    /**
     * Returns the Organization Unit to which the specified identifier is mapped.
     *
     * @param ouId the identifier whose associated Organization Unit is to be returned
     * @return the associated Organization Unit if found
     */
    @RequestMapping(value = "/{ou}", method = RequestMethod.GET)
    public ResponseEntity<List<OrganizationUnitResponse>> getOrganizationUnit(@PathVariable("ou") int ouId) {
        return getListResponseEntity(ouRepository.getOrganizationUnit(ouId));
    }

    /**
     * Returns the dependants of the Organization Unit to which the specified identifier is mapped.
     *
     * @param ouId the identifier whose associated Organization Unit is to be returned
     * @return the associated Organization Unit and its dependant Organization Units if found
     */
    @RequestMapping(value = "/{ouId}/dependants", method = RequestMethod.GET)
    public ResponseEntity<List<OrganizationUnitResponse>> getDependants(@PathVariable("ouId") int ouId) {
        return getListResponseEntity(ouRepository.getOrganizationUnitDependants(ouId));
    }

    /**
     * Replaces the Organization Unit <code>ou</code> current Reports-to-Relation with the Organization Unit
     * provided in <code>willReportToIdentifier</code>
     * <p>
     * This method does not replace the Reports-to-Relation if it creates a cycle. This cycle can be avoided
     * by first creating a new Reports-to-Relation for the Organization Unit.
     *
     * @param ou                     the Organization Unit to replace its Reports-to-Relation
     * @param willReportToIdentifier the Organization-Unit for creating the new Reports-to-Relation
     *                               (ou - Reports-to-Relation --> willReportTo.getOrganizationUnit())
     * @return the updated Organization Unit if success
     */
    @RequestMapping(value = "/{ou}/reports-to", method = RequestMethod.PUT)
    public ResponseEntity<List<OrganizationUnitResponse>> updateReportsTo(@PathVariable("ou") int ou,
                                                                          @RequestBody OrganizationUnitRequest willReportToIdentifier) {
        final int willReportTo = willReportToIdentifier.getOrganizationUnit();
        Map<Integer, OrganizationalRelation> organizationalRelations =
                ouRepository.getOrganizationalRelations(ou, willReportTo);
        ResponseEntity<List<OrganizationUnitResponse>> response = OK;

        if (isNotAReportsToRelationPair(organizationalRelations.values())) {
            response = NOT_FOUND;
        } else if (isRoot(organizationalRelations.get(ou))
                || isReportsToRelationACycle(organizationalRelations.get(ou), organizationalRelations.get(willReportTo))
                || organizationalRelations.get(ou).getReportsTo() == willReportTo) {
            response = BAD_REQUEST;
        }

        if (OK.equals(response)) {
            ouRepository.updateReportsTo(organizationalRelations.get(ou), organizationalRelations.get(willReportTo));
            response = getListResponseEntity(ouRepository.getOrganizationUnit(ou));
        }

        return response;
    }

    /**
     * Replaces the Organization Unit's Reports-To-Relation with a new Organization Unit. The height for
     * Organization Unit and its dependants will be now be height + 1.
     *
     * @param ou           the Organization Unit to replace its Reports-to-Relation
     * @param willReportTo the Organization-Unit for creating the new Reports-to-Relation
     *                     (ou - Reports-to-Relation --> willReportTo.getOrganizationUnit())
     * @return the created Organization Unit
     */
    @RequestMapping(value = "/{ou}/reports-to", method = RequestMethod.POST)
    public ResponseEntity<List<OrganizationUnitResponse>> createsReportsTo(@PathVariable("ou") int ou,
                                                                           @RequestBody OrganizationUnitRequest willReportTo) {
        List<OrganizationalRelation> organizationalRelation = ouRepository.getOrganizationalRelations(ou);
        ResponseEntity<List<OrganizationUnitResponse>> response = OK;

        if (organizationalRelation.isEmpty()) {
            response = NOT_FOUND;
        }

        if (OK.equals(response)) {
            OrganizationalRelation newRelation = ouRepository.createReportsTo(organizationalRelation.get(0));

            String[] path = newRelation.getPath().split(Pattern.quote("."));

            List<OrganizationUnitResponse> organizationUnitResponses = new ArrayList<>();
            organizationUnitResponses.add(new OrganizationUnitResponse(newRelation.getOrganizationUnit(),
                    newRelation.getReportsTo(), path.length - 1,
                    Integer.parseInt(path[0])));

            response = new ResponseEntity<>(organizationUnitResponses, HttpStatus.OK);
        }

        return response;
    }

    private ResponseEntity<List<OrganizationUnitResponse>> getListResponseEntity(
            final List<OrganizationUnitResponse> ouResponse) {
        HttpStatus httpStatus = HttpStatus.OK;

        if (ouResponse.isEmpty()) {
            httpStatus = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(ouResponse, httpStatus);
    }

}
