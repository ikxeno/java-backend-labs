package io.github.ikxeno.service;

import io.github.ikxeno.repository.CsvCharacterRepository;

import java.util.LinkedHashSet;

public class LocationService {
    private final CsvCharacterRepository characterRepository;

    public LocationService(CsvCharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public LinkedHashSet<String> findUniqueLocations() {
        LinkedHashSet<String> locations = new LinkedHashSet<>();
        characterRepository.forEach(character -> locations.add(character.locationName()));
        return locations;
    }
}
