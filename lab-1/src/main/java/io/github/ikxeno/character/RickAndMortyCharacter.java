package io.github.ikxeno.character;

import java.time.Instant;
import java.util.Objects;

public record RickAndMortyCharacter(
        int id,
        String name,
        String status,
        String species,
        String type,
        String gender,
        String originName,
        String locationName,
        Instant created
) {
    public RickAndMortyCharacter {
        if (id <= 0) {
            throw new IllegalArgumentException("Character ID must be positive");
        }
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(gender, "gender");
        Objects.requireNonNull(originName, "originName");
        Objects.requireNonNull(locationName, "locationName");
        Objects.requireNonNull(created, "created");
    }
}
