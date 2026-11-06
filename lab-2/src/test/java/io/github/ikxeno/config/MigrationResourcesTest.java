package io.github.ikxeno.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationResourcesTest {
    @Test
    void exposesSqlMigrationByRelativeAndFullPath() throws Exception {
        MigrationResources resources = new MigrationResources();
        var migration = resources.getResource("V1__create_airplane.sql");
        assertNotNull(migration);
        assertEquals(migration,
                resources.getResource("db/migration/V1__create_airplane.sql"));
        assertEquals(1,
                resources.getResources("V", new String[]{".sql"}).size());
        try (var reader = migration.read()) {
            assertTrue(reader.read() >= 0);
        }
    }

    @Test
    void doesNotReturnMigrationForCallbacksOrMissingResources() {
        MigrationResources resources = new MigrationResources();
        assertTrue(resources.getResources("beforeMigrate", new String[]{".sql"})
                .isEmpty());
        assertTrue(resources.getResources("V", new String[]{".txt"}).isEmpty());
        assertNull(resources.getResource("missing.sql"));
    }
}
