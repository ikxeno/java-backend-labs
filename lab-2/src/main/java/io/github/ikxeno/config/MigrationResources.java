package io.github.ikxeno.config;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class MigrationResources implements ResourceProvider {
    // Native images cannot enumerate classpath directories, so SQL resources
    // are registered explicitly.
    private final List<LoadableResource> resources = List
            .of(new ClassPathResource(
                    new Location("classpath:db/migration"),
                    "db/migration/V1__create_airplane.sql",
                    MigrationResources.class.getClassLoader(),
                    StandardCharsets.UTF_8));

    @Override
    public LoadableResource getResource(String name) {
        return resources.stream()
                .filter(resource -> resource.getRelativePath().equals(name)
                        || resource.getAbsolutePath().equals(name))
                .findFirst().orElse(null);
    }

    @Override
    public Collection<LoadableResource> getResources(String prefix,
            String[] suffixes) {
        return resources.stream()
                .filter(resource -> resource.getFilename().startsWith(prefix))
                .filter(resource -> Arrays.stream(suffixes)
                        .anyMatch(resource.getFilename()::endsWith))
                .toList();
    }
}
