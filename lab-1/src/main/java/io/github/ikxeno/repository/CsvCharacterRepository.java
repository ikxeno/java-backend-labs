package io.github.ikxeno.repository;

import io.github.ikxeno.character.RickAndMortyCharacter;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Objects;
import java.util.function.Consumer;

public class CsvCharacterRepository {
    private final Path file;

    public CsvCharacterRepository(Path file) {
        this.file = file.toAbsolutePath().normalize();
    }

    public List<RickAndMortyCharacter> findAll() {
        List<RickAndMortyCharacter> characters = new ArrayList<>();
        forEach(characters::add);
        return characters;
    }

    public void forEach(Consumer<RickAndMortyCharacter> action) {
        try (CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8, CharacterCsvMapper.readFormat())) {
            CharacterCsvMapper.validateHeader(parser.getHeaderNames());
            parser.stream()
                    .map(CharacterCsvMapper::parse)
                    .forEach(action);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read characters from " + file, exception);
        }
    }

    public Optional<RickAndMortyCharacter> findById(int id) {
        try (CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8, CharacterCsvMapper.readFormat())) {
            CharacterCsvMapper.validateHeader(parser.getHeaderNames());
            return parser.stream()
                    .map(CharacterCsvMapper::parse)
                    .filter(character -> character.id() == id)
                    .findFirst();
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read characters from " + file, exception);
        }
    }

    public void create(RickAndMortyCharacter character) {
        List<RickAndMortyCharacter> characters = findAll();
        if (characters.stream().anyMatch(existing -> existing.id() == character.id())) {
            throw new IllegalArgumentException("Character already exists: " + character.id());
        }
        characters.add(character);
        saveAll(characters);
    }

    public void update(RickAndMortyCharacter character) {
        List<RickAndMortyCharacter> characters = findAll();
        for (int index = 0; index < characters.size(); index++) {
            if (characters.get(index).id() == character.id()) {
                characters.set(index, character);
                saveAll(characters);
                return;
            }
        }
        throw new NoSuchElementException("Character does not exist: " + character.id());
    }

    public boolean deleteById(int id) {
        List<RickAndMortyCharacter> characters = findAll();
        boolean removed = characters.removeIf(character -> character.id() == id);
        if (removed) {
            saveAll(characters);
        }
        return removed;
    }

    private void saveAll(List<RickAndMortyCharacter> characters) {
        try {
            Path parent = Objects.requireNonNull(file.getParent(), "CSV path must have a parent directory");
            Path temporaryFile = Files.createTempFile(parent, "characters-", ".csv.tmp");
            try {
                writeCharacters(temporaryFile, characters);
                replaceFile(temporaryFile);
            } finally {
                Files.deleteIfExists(temporaryFile);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot save characters to " + file, exception);
        }
    }

    private void writeCharacters(Path destination, List<RickAndMortyCharacter> characters) throws IOException {
        try (var writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CharacterCsvMapper.writeFormat())) {
            for (RickAndMortyCharacter character : characters) {
                CharacterCsvMapper.print(printer, character);
            }
        }
    }

    private void replaceFile(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
