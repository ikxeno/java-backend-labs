package io.github.ikxeno;

import io.github.ikxeno.character.RickAndMortyCharacter;
import io.github.ikxeno.repository.CsvCharacterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvCharacterRepositoryTest {
    private static final Path SOURCE = Path.of("src/main/resources/characters.csv");
    private static final int TEST_CHARACTER_ID = 100;
    private static final Instant TEST_CREATED = Instant.parse("2026-01-01T00:00:00Z");
    private static final String CSV_HEADER = "id,name,status,species,type,gender,origin/name,location/name,created\n";
    private static final String VALID_ROW = "1,Rick,Alive,Human,,Male,Earth,Citadel,2026-01-01T00:00:00Z\n";

    @TempDir
    private Path directory;

    @ParameterizedTest(name = "CRUD preserves CSV text: {0}")
    @ValueSource(strings = {"Rick", "Рик, учёный", "Rick \"C-137\"", "Рик\nSmith", ""})
    void persistsCrudAndPreservesOtherCharacters(String text) throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("characters.csv"));
        var repository = new CsvCharacterRepository(input);
        var original = repository.findAll();
        var character = character(text, text);
        repository.create(character);
        assertEquals(character, new CsvCharacterRepository(input).findById(TEST_CHARACTER_ID).orElseThrow());
        var updated = character("Updated " + text, "Updated " + text);
        repository.update(updated);
        assertEquals(updated, new CsvCharacterRepository(input).findById(TEST_CHARACTER_ID).orElseThrow());
        assertEquals(original, repository.findAll().subList(0, original.size()));
        assertTrue(repository.deleteById(TEST_CHARACTER_ID));
        assertFalse(repository.deleteById(TEST_CHARACTER_ID));
        assertTrue(new CsvCharacterRepository(input).findById(TEST_CHARACTER_ID).isEmpty());
        assertEquals(original, repository.findAll());
    }

    @Test
    void rejectsDuplicateIdsAndMissingUpdatesWithoutChangingFile() throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("characters.csv"));
        var repository = new CsvCharacterRepository(input);
        byte[] original = Files.readAllBytes(input);
        assertThrows(IllegalArgumentException.class, () -> repository.create(repository.findAll().getFirst()));
        assertThrows(NoSuchElementException.class, () -> repository.update(character("Missing", "unknown")));
        assertArrayEquals(original, Files.readAllBytes(input));
    }

    @ParameterizedTest(name = "Reject malformed CSV: {0}")
    @MethodSource("malformedCsv")
    void rejectsMalformedCsv(String scenario, String contents, Class<? extends RuntimeException> exceptionType)
            throws IOException {
        Path input = directory.resolve("invalid.csv");
        Files.writeString(input, contents);
        var repository = new CsvCharacterRepository(input);
        assertAll(
                () -> assertThrows(exceptionType, repository::findAll),
                () -> assertThrows(exceptionType, () -> repository.findById(1)),
                () -> assertEquals(contents, Files.readString(input))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20})
    void findsFirstMiddleAndLastCharacters(int id) {
        assertEquals(id, new CsvCharacterRepository(SOURCE).findById(id).orElseThrow().id());
    }

    @Test
    void reportsMissingId() {
        assertTrue(new CsvCharacterRepository(SOURCE).findById(TEST_CHARACTER_ID).isEmpty());
    }

    @Test
    void stopsSearchingAfterMatchWithoutParsingFollowingRecords() throws IOException {
        Path input = directory.resolve("characters.csv");
        Files.writeString(input, CSV_HEADER + VALID_ROW + "2,\"unterminated\n");
        var repository = new CsvCharacterRepository(input);
        assertEquals(1, repository.findById(1).orElseThrow().id());
        assertThrows(UncheckedIOException.class, repository::findAll);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20})
    void updatePreservesPositionOfExistingCharacter(int id) throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("characters.csv"));
        var repository = new CsvCharacterRepository(input);
        var original = repository.findAll();
        var existing = repository.findById(id).orElseThrow();
        var updated = new RickAndMortyCharacter(id, "Updated", existing.status(), existing.species(),
                existing.type(), existing.gender(), existing.originName(), existing.locationName(), existing.created());
        var expected = new ArrayList<>(original);
        expected.set(original.indexOf(existing), updated);
        repository.update(updated);
        assertEquals(expected, new CsvCharacterRepository(input).findAll());
    }

    private static Stream<Arguments> malformedCsv() {
        return Stream.of(
                Arguments.of("empty file", "", IllegalArgumentException.class),
                Arguments.of("wrong header", "id,name\n1,Rick\n", IllegalArgumentException.class),
                Arguments.of("too few columns", CSV_HEADER + "1,Rick\n", IllegalArgumentException.class),
                Arguments.of("too many columns", CSV_HEADER + VALID_ROW.strip() + ",extra\n", IllegalArgumentException.class),
                Arguments.of("non-numeric ID", CSV_HEADER + VALID_ROW.replace("1,Rick", "abc,Rick"), IllegalArgumentException.class),
                Arguments.of("zero ID", CSV_HEADER + VALID_ROW.replace("1,Rick", "0,Rick"), IllegalArgumentException.class),
                Arguments.of("negative ID", CSV_HEADER + VALID_ROW.replace("1,Rick", "-1,Rick"), IllegalArgumentException.class),
                Arguments.of("invalid timestamp", CSV_HEADER + VALID_ROW.replace(TEST_CREATED.toString(), "yesterday"), IllegalArgumentException.class),
                Arguments.of("unterminated quote", CSV_HEADER + "1,\"Rick\n", UncheckedIOException.class)
        );
    }

    private RickAndMortyCharacter character(String name, String location) {
        return new RickAndMortyCharacter(TEST_CHARACTER_ID, name, "Alive", "Human", "", "Male", "unknown",
                location, TEST_CREATED);
    }
}
