package com.amazing.eng.companystructure.main;

import com.amazing.eng.companystructure.domain.OrganizationUnitRequest;
import com.amazing.eng.companystructure.domain.OrganizationUnitResponse;

import com.amazing.eng.companystructure.resources.ApiError;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompanyStructureApplicationTests {
    final static String VERSION_ROOT_URL = "/v1.0.0";
    final static String API_ROOT_URL = VERSION_ROOT_URL + "/organization-units";
    final static String DEPENDANTS_URL = API_ROOT_URL + "/{ou}/dependants";
    final static String REPORTS_TO_URL = API_ROOT_URL + "/{ou}/reports-to";
    final static String OU_URL = API_ROOT_URL + "/{ou}";
    final static String  INVALID_NON_INT_OU_URL = API_ROOT_URL + "/invalid-non-int-ou";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getDependants() {
        final Map<String, Integer> params = new HashMap<>();
        params.put("ou", 9);

        OrganizationUnitNodeList dependants = this.restTemplate.getForObject(DEPENDANTS_URL,
                OrganizationUnitNodeList.class, params);

        assertEquals(5, dependants.size());
        assertEquals(9, dependants.get(0).getOrganizationUnit());
        assertEquals(8, dependants.get(0).getReportsTo());
        assertEquals(3, dependants.get(0).getHeight());
        assertEquals(1, dependants.get(0).getRoot());
    }

    @Test
    public void updateReportsTo() {
        final int ou = 15;
        final int ouDependant = 20;
        final int willReportTo = 23;

        ResponseEntity<OrganizationUnitNodeList> updateResponse = this.restTemplate.exchange(
                REPORTS_TO_URL,
                HttpMethod.PUT,
                new HttpEntity<>(new OrganizationUnitRequest(willReportTo)),
                OrganizationUnitNodeList.class,
                ImmutableMap.of("ou", ou));

        ResponseEntity<OrganizationUnitNodeList> dependantResponse = this.restTemplate.exchange(
                OU_URL,
                HttpMethod.GET,
                new HttpEntity<>(""),
                OrganizationUnitNodeList.class,
                ImmutableMap.of("ou", ouDependant));

        final OrganizationUnitNodeList updated = updateResponse.getBody();
        final OrganizationUnitNodeList updatedDependant = dependantResponse.getBody();

        assertEquals(1, updated.size());
        assertEquals(5, updated.get(0).getHeight());
        assertEquals(6, updatedDependant.get(0).getHeight());
        assertEquals(1, updatedDependant.get(0).getRoot());
    }

    @Test
    public void createReportsTo() {
        final int ou = 143;

        ResponseEntity<OrganizationUnitNodeList> createResponse = this.restTemplate.exchange(
                REPORTS_TO_URL,
                HttpMethod.POST,
                new HttpEntity<>(new OrganizationUnitRequest(-1)),
                OrganizationUnitNodeList.class,
                ImmutableMap.of("ou", ou));

        final OrganizationUnitNodeList createdParent = createResponse.getBody();

        assertEquals(1, createdParent.size());

        ResponseEntity<OrganizationUnitNodeList> createdParentDependantsResponse = this.restTemplate.exchange(
                DEPENDANTS_URL,
                HttpMethod.GET,
                new HttpEntity<>(""),
                OrganizationUnitNodeList.class,
                ImmutableMap.of("ou", createdParent.get(0).getOrganizationUnit()));

        final OrganizationUnitNodeList createdParentDependants = createdParentDependantsResponse.getBody();

        assertEquals(3, createdParentDependants.size());
        assertEquals(160, createdParentDependants.get(1).getReportsTo());
        assertEquals(5, createdParentDependants.get(2).getHeight());
        assertEquals(1, createdParentDependants.get(2).getRoot());

    }

    @Test
    public void invalidOuForUpdate() {
        ResponseEntity<OrganizationUnitNodeList> updateResponse = this.restTemplate.exchange(
                REPORTS_TO_URL,
                HttpMethod.PUT,
                new HttpEntity<>(new OrganizationUnitRequest(-1)),
                OrganizationUnitNodeList.class,
                ImmutableMap.of("ou", -2));


        final OrganizationUnitNodeList response = updateResponse.getBody();

        assertEquals(HttpStatus.NOT_FOUND, updateResponse.getStatusCode());
        assertEquals(true, response.isEmpty());
    }

    @Test
    public void invalidOuForCreate() {
        ResponseEntity<OrganizationUnitNodeList> updateResponse = this.restTemplate.exchange(
                REPORTS_TO_URL,
                HttpMethod.POST,
                new HttpEntity<>(new OrganizationUnitRequest(-1)),
                OrganizationUnitNodeList.class,
                ImmutableMap.of("ou", -1));


        final OrganizationUnitNodeList response =  updateResponse.getBody();

        assertEquals(HttpStatus.NOT_FOUND, updateResponse.getStatusCode());
        assertEquals(true, response.isEmpty());
    }

    @Test
    public void invalidNonIntOuPath() {

        final ResponseEntity<ApiError> updateResponse = this.restTemplate.exchange(
                INVALID_NON_INT_OU_URL,
                HttpMethod.GET,
                new HttpEntity<>(""),
                ApiError.class);

        final ApiError apiError = updateResponse.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, apiError.getStatus());
    }
}

class OrganizationUnitNodeList extends ArrayList<OrganizationUnitResponse> {}
