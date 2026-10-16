package io.github.ikxeno.demo;

import io.github.ikxeno.character.RickAndMortyCharacter;
import io.github.ikxeno.repository.CsvCharacterRepository;

import java.io.PrintStream;
import java.time.Instant;

public class CrudDemo {
    private final CsvCharacterRepository characterRepository;
    private final PrintStream output;

    public CrudDemo(CsvCharacterRepository characterRepository) {
        this(characterRepository, System.out);
    }

    public CrudDemo(CsvCharacterRepository characterRepository, PrintStream output) {
        this.characterRepository = characterRepository;
        this.output = output;
    }

    public void run() {
        int maximumId = characterRepository.findAll().stream()
                .mapToInt(RickAndMortyCharacter::id)
                .max()
                .orElse(0);
        int characterId = Math.incrementExact(maximumId);
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        var character = new RickAndMortyCharacter(characterId, "Demo Rick", "Alive", "Human", "", "Male",
                "Earth (C-137)", "Citadel of Ricks", created);
        characterRepository.create(character);
        output.println("Created: " + characterRepository.findById(characterId).orElseThrow());

        var updatedCharacter = new RickAndMortyCharacter(characterId, character.name(), character.status(),
                character.species(), character.type(), character.gender(), character.originName(),
                "Earth (Replacement Dimension)", character.created());
        characterRepository.update(updatedCharacter);
        output.println("Updated: " + characterRepository.findById(characterId).orElseThrow());

        output.println("Deleted: " + characterRepository.deleteById(characterId));
        output.println("Present after deletion: " + characterRepository.findById(characterId).isPresent());
    }
}
