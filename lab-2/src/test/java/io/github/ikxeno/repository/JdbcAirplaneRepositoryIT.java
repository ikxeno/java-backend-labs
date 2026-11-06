package io.github.ikxeno.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.ikxeno.airplane.AirplaneEntity;
import io.github.ikxeno.config.MigrationResources;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class JdbcAirplaneRepositoryIT {
    private static final String SCHEMA = "repository_test";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            "postgres:18-alpine")
            .withDatabaseName("airplanes")
            .withUsername("labs")
            .withPassword("labs");

    private static HikariDataSource dataSource;
    private static Flyway flyway;

    private AirplaneRepository repository;

    @BeforeAll
    static void openDatabase() {
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(POSTGRES.getJdbcUrl());
        configuration.setUsername(POSTGRES.getUsername());
        configuration.setPassword(POSTGRES.getPassword());
        configuration.setSchema(SCHEMA);
        configuration.setMaximumPoolSize(4);
        configuration.setConnectionTimeout(1000);
        dataSource = new HikariDataSource(configuration);
        flyway = Flyway.configure().dataSource(dataSource).schemas(SCHEMA)
                .defaultSchema(SCHEMA)
                .resourceProvider(new MigrationResources()).load();
        flyway.migrate();
    }

    @AfterAll
    static void closeDatabase() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @BeforeEach
    void clearAirplanes() throws SQLException {
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE airplane RESTART IDENTITY");
        }
        repository = new JdbcAirplaneRepository(dataSource);
    }

    @Test
    void crudPersistsDataAndRepeatedUpdatesAndDeletesAreIdempotent() {
        int id = repository.save(new AirplaneEntity(null, "Airbus A320", 180));
        AirplaneEntity saved = new AirplaneEntity(id, "Airbus A320", 180);
        assertTrue(id > 0);
        assertEquals(saved, repository.findById(id));
        assertEquals(saved, repository.findByModel(saved.model()));
        assertEquals(List.of(saved), repository.findAll());

        int otherId = repository
                .save(new AirplaneEntity(null, "Boeing 737", 189));
        AirplaneEntity other = new AirplaneEntity(otherId, "Boeing 737", 189);

        AirplaneEntity updated = new AirplaneEntity(id, "Airbus A320neo", 194);
        assertTrue(repository.update(updated));
        assertTrue(repository.update(updated));
        assertEquals(List.of(updated, other), repository.findAll());
        assertNull(repository.findByModel(saved.model()));

        repository.deleteById(id);
        repository.deleteById(id);
        assertNull(repository.findById(id));
        assertEquals(List.of(other), repository.findAll());
        assertFalse(repository.update(updated));
    }

    @Test
    void absentRecordsHaveDefinedResults() {
        assertNull(repository.findById(1));
        assertNull(repository.findByModel("missing"));
        assertFalse(repository.update(new AirplaneEntity(1, "missing", 1)));
        repository.deleteById(1);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void duplicateModelsGetDifferentIdsAndSearchReturnsFirst() {
        AirplaneEntity draft = new AirplaneEntity(null, "Boeing 737", 189);
        int first = repository.save(draft);
        int second = repository.save(draft);
        assertTrue(second > first);
        assertEquals(first, repository.findByModel(draft.model()).id());
        assertEquals(List.of(
                new AirplaneEntity(first, draft.model(), draft.seats()),
                new AirplaneEntity(second, draft.model(), draft.seats())),
                repository.findAll());
    }

    @Test
    void quotedAndUnicodeModelsAreStoredAsData() {
        String model = "Ту-204 '); DROP TABLE airplane; --";
        int id = repository.save(new AirplaneEntity(null, model, 210));
        assertEquals(new AirplaneEntity(id, model, 210),
                repository.findByModel(model));
        assertNull(repository.findByModel("' OR 1=1 --"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void migrationsCanBeRepeatedWithoutLosingData() {
        int id = repository.save(new AirplaneEntity(null, "МС-21", 211));
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertNotNull(new JdbcAirplaneRepository(dataSource).findById(id));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void databaseConstraintRejectsInvalidUpdateAtomically(int seats)
            throws SQLException {
        int id = repository.save(new AirplaneEntity(null, "Airbus", 180));
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "UPDATE airplane SET model = ?, seats = ? "
                                + "WHERE id = ?")) {
            statement.setString(1, "invalid update");
            statement.setInt(2, seats);
            statement.setInt(3, id);
            assertThrows(SQLException.class, statement::executeUpdate);
        }
        assertEquals(new AirplaneEntity(id, "Airbus", 180),
                repository.findById(id));
    }

    @Test
    void connectionFailureIsPropagatedWithItsCause() {
        HikariDataSource closedPool = new HikariDataSource();
        closedPool.close();
        AirplaneRepository broken = new JdbcAirplaneRepository(closedPool);
        RepositoryException exception = assertThrows(RepositoryException.class,
                broken::findAll);
        assertTrue(exception.getCause() instanceof SQLException);
    }
}
