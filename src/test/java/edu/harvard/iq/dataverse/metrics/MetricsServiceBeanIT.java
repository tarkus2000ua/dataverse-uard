package edu.harvard.iq.dataverse.metrics;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.db.migration.SharedPostgresContainer;
import edu.harvard.iq.dataverse.util.testing.Tags;
import jakarta.json.JsonArray;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static edu.harvard.iq.dataverse.makedatacount.MakeDataCountUtil.MetricType.VIEWS_TOTAL_REGULAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Executes the service's actual native SQL against isolated PostgreSQL fixtures. */
@Tag(Tags.INTEGRATION_TEST)
@Tag(Tags.USES_TESTCONTAINERS)
@Testcontainers(disabledWithoutDocker = true)
class MetricsServiceBeanIT {
    private Connection connection;
    private MetricsServiceBean service;

    @BeforeEach
    void setUp() throws Exception {
        connection = SharedPostgresContainer.getConnection();
        execute("""
                CREATE TEMP TABLE dvobject (id bigint, dtype text, owner_id bigint, publicationdate timestamp);
                CREATE TEMP TABLE dataverse (id bigint);
                CREATE TEMP TABLE datasetmetrics
                    (dataset_id bigint, monthyear varchar(255), countrycode varchar(255), viewstotalregular bigint);
                INSERT INTO dvobject VALUES
                    (3000000000, 'Dataverse', null, '2020-01-01'),
                    (3000000001, 'Dataverse', 3000000000, '2020-01-01'),
                    (3000000002, 'Dataverse', null, '2020-01-01'),
                    (3000000003, 'Dataverse', null, '2020-01-01'),
                    (4000000000, 'Dataset', 3000000000, '2020-01-01'),
                    (4000000001, 'Dataset', 3000000001, '2020-01-01'),
                    (4000000002, 'Dataset', 3000000002, '2020-01-01'),
                    (4000000003, 'Dataset', 3000000000, null),
                    (5000000000, 'DataFile', 4000000000, '2020-01-01');
                INSERT INTO dataverse SELECT id FROM dvobject WHERE dtype = 'Dataverse';
                """);
        EntityManager em = mock(EntityManager.class);
        // Keep the PostgreSQL numeric result types, including Long for bigint IDs.
        when(em.createNativeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            Query query = mock(Query.class);
            when(query.getResultList()).thenAnswer(ignored -> queryRows(sql));
            when(query.getSingleResult()).thenAnswer(ignored -> queryRows(sql).get(0));
            return query;
        });
        service = new MetricsServiceBean();
        Field field = MetricsServiceBean.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(service, em);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            connection.close(); // All fixture tables are session-local.
        }
    }

    @ParameterizedTest
    @CsvSource({"2025-12,1", "2026-01,31", "2026-02,127", "2026-12,127", "2027-01,255"})
    void includesEntireRequestedMonthAndExcludesFollowingMonth(String month, long expected) throws Exception {
        execute("""
                INSERT INTO datasetmetrics VALUES
                    (4000000000, '2025-12-31', 'ua', 1),
                    (4000000000, '2026-01', 'ua', 2),
                    (4000000000, '2026-01-01', 'ua', 4),
                    (4000000000, '2026-01-31', 'ua', 8),
                    (4000000000, '2026-01-31T23:59:59', 'ua', 16),
                    (4000000000, '2026-02', 'ua', 32),
                    (4000000000, '2026-02-01', 'ua', 64),
                    (4000000000, '2027-01-01', 'ua', 128);
                """);
        assertEquals(expected, total(month, null, null));
        assertEquals(expected, total(month, "ua", collection(3000000000L)));
    }

    @Test
    void filtersPublishedDatasetsRecursivelyWithBigintIdsAndCountry() throws Exception {
        seedSubtreeMetrics();
        Dataverse root = collection(3000000000L);
        assertEquals(3, total("2020-01", null, root));
        assertEquals(1, total("2020-01", "ua", root));
        assertEquals(2, total("2020-01", null, collection(3000000001L)));
        assertEquals(4, total("2020-01", null, collection(3000000002L)));
        assertEquals(31, total("2020-01", null, null)); // Existing unfiltered semantics.
        JsonArray monthly = service.mdcMetricTimeSeries(VIEWS_TOTAL_REGULAR, null, root);
        assertEquals(3, monthly.getJsonObject(0).getInt("count"));
        assertEquals(3, monthly.getJsonObject(monthly.size() - 1).getInt("count"));
        assertEquals(1, service.mdcMetricTimeSeries(VIEWS_TOTAL_REGULAR, "ua", root)
                .getJsonObject(0).getInt("count"));
    }

    @Test
    void emptyCollectionAndAbsentCountryReturnZeroAndEmptyTimeSeries() throws Exception {
        seedSubtreeMetrics();
        Dataverse empty = collection(3000000003L);
        assertEquals(0, total("2020-01", null, empty));
        assertEquals(0, service.mdcMetricTimeSeries(VIEWS_TOTAL_REGULAR, null, empty).size());
        Dataverse root = collection(3000000000L);
        assertEquals(0, total("2020-01", "fr", root));
        assertEquals(0, service.mdcMetricTimeSeries(VIEWS_TOTAL_REGULAR, "fr", root).size());
    }

    @Test
    void sharedHelperStillIncludesReleasedParentForDataverseQueries() {
        assertEquals(1, service.dataversesToMonth("2020-01", collection(3000000000L)));
        assertEquals(0, service.dataversesToMonth("2020-01", collection(3000000003L)));
        assertEquals(2, service.getDataversesTimeSeries(null, collection(3000000000L))
                .getJsonObject(0).getJsonNumber("count").longValue());
    }

    private void seedSubtreeMetrics() throws Exception {
        execute("""
                INSERT INTO datasetmetrics VALUES
                    (4000000000, '2020-01-01', 'ua', 1),
                    (4000000001, '2020-01', 'us', 2),
                    (4000000002, '2020-01-31', 'ua', 4),
                    (4000000003, '2020-01-01', 'ua', 8),
                    (5000000000, '2020-01-01', 'ua', 16);
                """);
    }

    private long total(String month, String country, Dataverse parent) {
        return service.getMDCDatasetMetrics(VIEWS_TOTAL_REGULAR, month, country, parent)
                .getJsonNumber(VIEWS_TOTAL_REGULAR.toString()).longValue();
    }

    private Dataverse collection(long id) {
        Dataverse dataverse = mock(Dataverse.class);
        when(dataverse.getId()).thenReturn(id);
        when(dataverse.isReleased()).thenReturn(true);
        return dataverse;
    }

    private void execute(String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private List<Object> queryRows(String sql) throws Exception {
        List<Object> rows = new ArrayList<>();
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            int columns = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                if (columns == 1) {
                    rows.add(rs.getObject(1));
                } else {
                    Object[] row = new Object[columns];
                    for (int i = 0; i < columns; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
