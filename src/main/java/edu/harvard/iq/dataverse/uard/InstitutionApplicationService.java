package edu.harvard.iq.dataverse.uard;

import edu.harvard.iq.dataverse.authorization.users.User;
import edu.harvard.iq.dataverse.util.json.JsonUtil;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

/** Single-statement JDBC writes are atomic, including the logo and notification queue. */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InstitutionApplicationService {
    @Resource(lookup = "java:app/jdbc/dataverse")
    DataSource dataSource;

    public void save(UUID id, InstitutionApplicationValidator.Validated application) throws SQLException {
        var json = JsonUtil.createObjectBuilder();
        InstitutionApplicationValidator.FIELDS.forEach(key -> json.add(key, application.fields().get(key)));
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement("""
                INSERT INTO uard_institution_application (id, fields, logo, logo_type, consent_version)
                VALUES (?, ?, ?, ?, '2026-11-25') ON CONFLICT (id) DO NOTHING
                """)) {
            statement.setObject(1, id);
            statement.setString(2, json.build().toString());
            statement.setBytes(3, application.logo());
            statement.setString(4, application.logoType());
            statement.executeUpdate();
        }
    }

    public List<Application> list(User user, int offset) throws SQLException {
        requireAdmin(user);
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement("""
                SELECT id, created_at, fields, notification_sent_at, notification_attempts
                FROM uard_institution_application ORDER BY created_at DESC, id LIMIT 51 OFFSET ?
                """)) {
            statement.setInt(1, Math.max(0, offset));
            try (var result = statement.executeQuery()) {
                List<Application> applications = new ArrayList<>();
                while (result.next()) applications.add(read(result));
                return applications;
            }
        }
    }

    public Application get(User user, UUID id) throws SQLException {
        requireAdmin(user);
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement("""
                SELECT id, created_at, fields, notification_sent_at, notification_attempts
                FROM uard_institution_application WHERE id = ?
                """)) {
            statement.setObject(1, id);
            try (var result = statement.executeQuery()) { return result.next() ? read(result) : null; }
        }
    }

    public record Logo(byte[] bytes, String type) { }
    public Logo logo(User user, UUID id) throws SQLException {
        requireAdmin(user);
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(
                "SELECT logo, logo_type FROM uard_institution_application WHERE id = ?")) {
            statement.setObject(1, id);
            try (var result = statement.executeQuery()) {
                return result.next() ? new Logo(result.getBytes(1), result.getString(2)) : null;
            }
        }
    }

    public List<UUID> pending() throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement("""
                SELECT id FROM uard_institution_application WHERE notification_sent_at IS NULL
                AND notification_next_attempt <= CURRENT_TIMESTAMP ORDER BY created_at LIMIT 10
                """); var result = statement.executeQuery()) {
            List<UUID> ids = new ArrayList<>();
            while (result.next()) ids.add(result.getObject(1, UUID.class));
            return ids;
        }
    }

    public void notificationResult(UUID id, boolean sent) throws SQLException {
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement("""
                UPDATE uard_institution_application SET notification_attempts = notification_attempts + 1,
                notification_sent_at = CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END,
                notification_next_attempt = CURRENT_TIMESTAMP + INTERVAL '5 minutes'
                WHERE id = ? AND notification_sent_at IS NULL
                """)) {
            statement.setBoolean(1, sent);
            statement.setObject(2, id);
            statement.executeUpdate();
        }
    }

    static void requireAdmin(User user) {
        if (user == null || !user.isAuthenticated() || !user.isSuperuser()) throw new SecurityException();
    }

    private static Application read(ResultSet result) throws SQLException {
        Map<String, String> fields = new LinkedHashMap<>();
        var json = JsonUtil.getJsonObject(result.getString("fields"));
        InstitutionApplicationValidator.FIELDS.forEach(key -> fields.put(key, json.getString(key)));
        Timestamp sent = result.getTimestamp("notification_sent_at");
        return new Application(result.getObject("id", UUID.class), result.getTimestamp("created_at").toInstant(),
                fields, sent != null, result.getInt("notification_attempts"));
    }

    public static class Application implements java.io.Serializable {
        private final UUID id;
        private final Instant created;
        private final Map<String, String> fields;
        private final boolean notified;
        private final int attempts;
        Application(UUID id, Instant created, Map<String, String> fields, boolean notified, int attempts) {
            this.id = id; this.created = created; this.fields = fields; this.notified = notified; this.attempts = attempts;
        }
        public UUID getId() { return id; }
        public String getCreated() { return created.toString(); }
        public Map<String, String> getFields() { return fields; }
        public boolean isNotified() { return notified; }
        public int getAttempts() { return attempts; }
    }
}
