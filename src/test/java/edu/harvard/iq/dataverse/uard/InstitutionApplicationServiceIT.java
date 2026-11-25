package edu.harvard.iq.dataverse.uard;

import edu.harvard.iq.dataverse.authorization.users.User;
import edu.harvard.iq.dataverse.db.migration.SharedPostgresContainer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers(disabledWithoutDocker = true)
class InstitutionApplicationServiceIT {
    static InstitutionApplicationService service;
    static User admin;

    @BeforeAll static void database() throws Exception {
        var container = SharedPostgresContainer.getInstance();
        var source = new PGSimpleDataSource();
        source.setURL(container.getJdbcUrl());
        source.setUser(container.getUsername());
        source.setPassword(container.getPassword());
        service = new InstitutionApplicationService(); service.dataSource = source;
        try (var connection = source.getConnection(); var statement = connection.createStatement();
                var migration = InstitutionApplicationServiceIT.class.getResourceAsStream("/db/migration/V6.12.0.1__uard_institution_applications.sql")) {
            statement.execute(new String(migration.readAllBytes(), StandardCharsets.UTF_8));
        }
        admin = mock(User.class); when(admin.isAuthenticated()).thenReturn(true); when(admin.isSuperuser()).thenReturn(true);
    }
    @BeforeEach void clearOwnTestTable() throws Exception {
        try (var connection = service.dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("TRUNCATE uard_institution_application");
        }
    }
    @Test void savesFieldsLogoConsentAndQueueAtomicallyAndReplaysWithoutOverwrite() throws Exception {
        UUID id = UUID.randomUUID(); byte[] logo = InstitutionApplicationValidatorTest.image("png");
        var original = InstitutionApplicationValidatorTest.fields(); original.put("nameUk", "Тестовий університет");
        service.save(id, new InstitutionApplicationValidator.Validated(original, logo, "image/png"));
        service.save(id, new InstitutionApplicationValidator.Validated(InstitutionApplicationValidatorTest.fields(), logo, "image/png"));
        assertEquals(1, service.list(admin, 0).size());
        assertEquals("Тестовий університет", service.get(admin, id).getFields().get("nameUk"));
        assertArrayEquals(logo, service.logo(admin, id).bytes());
        assertEquals(java.util.List.of(id), service.pending());
        assertFalse(service.get(admin, id).isNotified());
        service.notificationResult(id, false);
        assertTrue(service.pending().isEmpty());
        assertEquals(1, service.get(admin, id).getAttempts());
        service.notificationResult(id, true);
        assertTrue(service.get(admin, id).isNotified());
    }
    @Test void concurrentRetriesCreateOneRowAndDatabaseRejectsIncompleteLogo() throws Exception {
        UUID id = UUID.randomUUID();
        var application = new InstitutionApplicationValidator.Validated(InstitutionApplicationValidatorTest.fields(),
                InstitutionApplicationValidatorTest.image("png"), "image/png");
        try (var executor = Executors.newFixedThreadPool(4)) {
            var tasks = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int index = 0; index < 8; index++) tasks.add(executor.submit(() -> { service.save(id, application); return null; }));
            for (var task : tasks) task.get();
        }
        assertEquals(1, service.list(admin, 0).size());
        UUID invalid = UUID.randomUUID();
        assertThrows(SQLException.class, () -> service.save(invalid, new InstitutionApplicationValidator.Validated(
                InstitutionApplicationValidatorTest.fields(), new byte[0], "image/png")));
        assertNull(service.get(admin, invalid));
        assertEquals(1, service.list(admin, 0).size());
    }
    @Test void guestAndOrdinaryUsersCannotReadFieldsOrLogos() {
        User ordinary = mock(User.class); when(ordinary.isAuthenticated()).thenReturn(true);
        for (User user : new User[]{null, mock(User.class), ordinary}) {
            assertThrows(SecurityException.class, () -> service.list(user, 0));
            assertThrows(SecurityException.class, () -> service.get(user, UUID.randomUUID()));
            assertThrows(SecurityException.class, () -> service.logo(user, UUID.randomUUID()));
        }
    }
}
