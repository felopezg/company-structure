package com.amazing.eng.companystructure.database;

import com.amazing.eng.companystructure.domain.OrganizationUnitResponse;
import com.amazing.eng.companystructure.domain.OrganizationalRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.valueOf;

@Repository
public class OrganizationalStructureRepository {
    private static final Predicate<String> NOT_NULL_OR_EMPTY
            = str -> str != null && !str.isEmpty();
    private static final String SELECT_NEXT_OU_ID = "SELECT NEXTVAL('ou_seq')";
    private static final String INSERT_NEW_OU =
            "INSERT INTO organization_units (id, name, description) VALUES (?, ?, ?)";
    private static final String INSERT_INTO_ORGANIZATIONAL_STRUCTURE =
            "INSERT INTO organizational_structure (organization_unit, reports_to, path) VALUES (?, ?, ?)";
    /**
     * SQL statement for retrieving the Organizational-Relations needed for updating an Organization Unit's
     * Reports-to-Relation
     */
    private static final String SELECT_DO_OUS_FOR_PARENT_CHANGE_EXIST =
            "SELECT organization_unit, reports_to, path, "
                    + "LENGTH(REGEXP_REPLACE(path,'[^.]','')) - 1 AS height,"
                    + "CAST(LEFT(path, LOCATE('.', path,  1) - 1) AS INT) AS root "
                    + "FROM organizational_structure "
                    + "WHERE organization_unit IN (?,?)";
    /**
     * SQL statement for updating an Organization Unit's dependants node data.
     * The height of each node is calculated from the node's organization units enumeration column (path).
     */
    private static final String SELECT_OU_DEPENDANTS =
            "SELECT organization_unit, reports_to, LENGTH(REGEXP_REPLACE(path,'[^.]','')) - 1 AS height, "
                    + "CAST(LEFT(path, LOCATE('.', path,  1) - 1) AS INT) AS root "
                    + "FROM organizational_structure "
                    + "WHERE path LIKE  (SELECT path FROM organizational_structure WHERE organization_unit = ?) || '%' "
                    + "ORDER BY path";
    /**
     * SQL statement for retrieving an Organization Unit Node's data.
     * The height of the node is calculated from the node's organization units enumeration column (path).
     */
    private static final String SELECT_OU =
            "SELECT organization_unit, reports_to, path, LENGTH(REGEXP_REPLACE(path,'[^.]',''))-1 AS height, "
                    + "CAST(LEFT(path, LOCATE('.', path,  1) - 1) AS INT) AS root "
                    + "FROM organizational_structure "
                    + "WHERE organization_unit =  ?  ";

    private static final String SELECT_OU_REPORTS_TO_PATH =
            "SELECT organization_unit, reports_to, path, SUBSTRING(path, 1, LOCATE('.', path,  -2)) reports_to_path, "
                    + "LENGTH(REGEXP_REPLACE(path,'[^.]',''))-1 AS height, "
                    + "CAST(LEFT(path, LOCATE('.', path,  1) - 1) AS INT) AS root "
                    + "FROM organizational_structure "
                    + "WHERE organization_unit  = ?";
    /**
     * SQL statement for updating an Organization Unit's Reports-to-Relation.
     */
    private static final String UPDATE_OU_REPORTS_TO = "UPDATE organizational_structure "
            + "SET reports_to = ? "
            + "WHERE organization_unit = ?";
    /**
     * SQL statement for updating an Organization Unit's relations with itself and its dependants.
     */
    private static final String UPDATE_OU_CHILDREN = "UPDATE organizational_structure "
            + "SET path = REGEXP_REPLACE(path, "
            + "                   ?, "
            + "                   ?) "
            + "WHERE path like  ?  || '%'";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retrieves the Organizational-Relations needed for updating an Organization Unit's Reports-to-Relation
     *
     * @return a map containing all the relations found.
     */
    public Map<Integer, OrganizationalRelation> getOrganizationalRelations(int ou, int willReportTo) {
        return jdbcTemplate.query(SELECT_DO_OUS_FOR_PARENT_CHANGE_EXIST,
                new Object[]{ou, willReportTo},
                (ResultSet rs) -> {
                    Map<Integer, OrganizationalRelation> relations = new HashMap<>();

                    while (rs.next()) {
                        OrganizationalRelation relation = new OrganizationalRelation(rs.getInt("organization_unit"),
                                rs.getInt("reports_to"),
                                rs.getString("path"));
                        relation.setHeight(rs.getInt("height"));
                        relation.setRoot(rs.getInt("root"));
                        relations.put(relation.getOrganizationUnit(), relation);
                    }

                    return relations;
                });
    }

    /**
     * Retrieves the Organizational-Relations needed for replacing an Organization Unit's Reports-to-Relation
     *
     * @return an Optional containing all the relations found.
     */
    public List<OrganizationalRelation> getOrganizationalRelations(int ou) {
        return jdbcTemplate.query(SELECT_OU_REPORTS_TO_PATH,
                new Object[]{ou},
                (rs, i) -> new OrganizationalRelation(rs.getInt("organization_unit"),
                        rs.getInt("reports_to"),
                        rs.getString("path"),
                        rs.getString("reports_to_path"),
                        rs.getInt("height"),
                        rs.getInt("root")));
    }

    /**
     * Retrieves an Organization Unit's dependant(s).
     *
     * @return if found a list containing the Organization Unit´s dependants; if not found an empty list.
     */
    public List<OrganizationUnitResponse> getOrganizationUnitDependants(int ou) {
        return jdbcTemplate.query(SELECT_OU_DEPENDANTS,
                new Object[]{ou},
                (ResultSet rs, int rowIndex) -> new OrganizationUnitResponse(
                        rs.getInt("organization_unit"),
                        rs.getInt("reports_to"),
                        rs.getInt("height"),
                        rs.getInt("root")));
    }

    /**
     * Retrieves an Organization Unit.
     *
     * @return if found a list containing the Organization Unit; if not found an empty list.
     */
    public List<OrganizationUnitResponse> getOrganizationUnit(int ou) {
        return jdbcTemplate.query(SELECT_OU,
                new Object[]{ou},
                (ResultSet rs, int rowIndex) -> new OrganizationUnitResponse(
                        rs.getInt("organization_unit"),
                        rs.getInt("reports_to"),
                        rs.getInt("height"),
                        rs.getInt("root")));
    }

    /**
     * Updates the Reports-to-Relation of an Organization Unit.
     *
     * @return the number of Organization Units affected
     */
    @Transactional
    public OrganizationUnitResponse updateReportsTo(final OrganizationalRelation ouRelation,
                                                    final OrganizationalRelation willReportToRelation) {
        Assert.notNull(ouRelation, "Organization Unit Self-Relation must not be null.");
        Assert.notNull(ouRelation, "Organization Unit Reports-to-Relation must not be null.");
        final String ouUpdatedPath = willReportToRelation.getPath() + ouRelation.getOrganizationUnit() + ".";
        int result = jdbcTemplate.update(UPDATE_OU_REPORTS_TO,
                willReportToRelation.getOrganizationUnit(),
                ouRelation.getOrganizationUnit());

        result += jdbcTemplate.update(UPDATE_OU_CHILDREN,
                "^" + Pattern.quote(ouRelation.getPath()),
                ouUpdatedPath,
                ouRelation.getPath());

        if (this.logger.isDebugEnabled()) {
            this.logger.debug(String.format("Updated relation [%s, %s]", ouRelation.getPath(), ouUpdatedPath));
            this.logger.debug(String.format("Updated records [%d]", result));
        }

        return new OrganizationUnitResponse(ouRelation.getOrganizationUnit(), willReportToRelation.getOrganizationUnit(),
                willReportToRelation.getHeight() + 1, ouRelation.getRoot());
    }


    /**
     * Creates a Reports-to-Relation for an Organization Unit. Organization Unit and its dependant Organization
     * Unit will have height + 1 after the Organization Unit Reports-to-Relation is updated.
     *
     * @return the created organizational relation
     */
    @Transactional
    public OrganizationUnitResponse createReportsTo(final OrganizationalRelation ouRelation) {
        final int newOuId = jdbcTemplate.queryForObject(SELECT_NEXT_OU_ID, Integer.class);
        final String newOuPath = Stream.of(ouRelation.getReportsToPath(), valueOf(newOuId))
                .filter(NOT_NULL_OR_EMPTY)
                .collect(Collectors.joining("")) + ".";
        final int root = ouRelation.getReportsTo() == -1 ? newOuId : ouRelation.getRoot();


        int result = jdbcTemplate.update(INSERT_NEW_OU, newOuId, valueOf(newOuId), valueOf(newOuId));

        result += jdbcTemplate.update(INSERT_INTO_ORGANIZATIONAL_STRUCTURE, newOuId, ouRelation.getReportsTo(),
                newOuPath);

        result += jdbcTemplate.update(UPDATE_OU_REPORTS_TO,
                newOuId,
                ouRelation.getOrganizationUnit());


        result += jdbcTemplate.update(UPDATE_OU_CHILDREN, "^" + Pattern.quote(ouRelation.getPath()),
                newOuPath + ouRelation.getOrganizationUnit() + ".",
                ouRelation.getPath());

        if (this.logger.isDebugEnabled()) {
            this.logger.debug(String.format("New relation [%d, %s]", newOuId, newOuPath));
            this.logger.debug(String.format("Updated records [%d]", result));
        }

        return new OrganizationUnitResponse(newOuId, ouRelation.getReportsTo(), ouRelation.getHeight(), root);
    }

}
