package io.github.ikxeno;

import io.github.ikxeno.application.LabApplication;
import io.github.ikxeno.cli.CliArguments;
import io.github.ikxeno.demo.CrudDemo;
import io.github.ikxeno.repository.CsvCharacterRepository;
import io.github.ikxeno.service.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTest {
    private static final Path SOURCE = Path.of("src/main/resources/characters.csv");
    private static final List<String> EXPECTED_LOCATIONS = List.of(
            "Citadel of Ricks", "Earth (Replacement Dimension)", "Abadango", "Testicle Monster Dimension",
            "Worldender's lair", "Anatomy Park", "unknown", "Interdimensional Cable"
    );

    @TempDir
    private Path directory;

    @ParameterizedTest(name = "Export official dataset, CRUD demo = {0}")
    @ValueSource(booleans = {false, true})
    void exportsOfficialDatasetAndPreservesOriginalCharacters(boolean crudDemo) throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("персонажи demo.csv"));
        Path output = directory.resolve("nested/locations.txt");
        var original = new CsvCharacterRepository(input).findAll();
        byte[] originalBytes = Files.readAllBytes(input);
        var arguments = new ArrayList<>(List.of(input.toString(), output.toString()));
        if (crudDemo) {
            arguments.add("--crud-demo");
        }
        execute(arguments.toArray(String[]::new));
        assertEquals(EXPECTED_LOCATIONS, Files.readAllLines(output));
        assertEquals(original, new CsvCharacterRepository(input).findAll());
        if (!crudDemo) {
            assertArrayEquals(originalBytes, Files.readAllBytes(input));
        }
    }

    @ParameterizedTest(name = "Extraction: {0}")
    @MethodSource("locationScenarios")
    void preservesFirstOccurrenceOrder(String scenario, List<String> locations, List<String> expected)
            throws IOException {
        Path input = directory.resolve("characters.csv");
        String header = Files.readAllLines(SOURCE).getFirst();
        var contents = new StringBuilder(header).append('\n');
        for (int index = 0; index < locations.size(); index++) {
            contents.append(index + 1).append(",Rick,Alive,Human,,Male,Origin,")
                    .append(locations.get(index)).append(",2026-01-01T00:00:00Z\n");
        }
        Files.writeString(input, contents);
        Path output = directory.resolve("locations.txt");
        execute(new String[]{input.toString(), output.toString()});
        assertEquals(expected, Files.readAllLines(output));
    }

    @Test
    void seesRepositoryChangesAndDoesNotCachePreviousResults() throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("characters.csv"));
        var repository = new CsvCharacterRepository(input);
        var service = new LocationService(repository);
        var previous = service.findUniqueLocations();
        var first = repository.findAll().getFirst();
        repository.deleteById(first.id());
        repository.deleteById(repository.findAll().getFirst().id());
        assertEquals("Earth (Replacement Dimension)", service.findUniqueLocations().getFirst());
        assertEquals(EXPECTED_LOCATIONS, List.copyOf(previous));
    }

    @Test
    void crudDemoCanBeRepeatedWithoutLeavingCharactersBehind() throws IOException {
        var repository = new CsvCharacterRepository(Files.copy(SOURCE, directory.resolve("characters.csv")));
        var original = repository.findAll();
        var demo = new CrudDemo(repository);
        demo.run();
        demo.run();
        assertEquals(original, repository.findAll());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void acceptsCrudFlagBeforeBetweenAndAfterPaths(int flagPosition) {
        var arguments = new ArrayList<>(List.of("input.csv", "output.txt"));
        arguments.add(flagPosition, "--crud-demo");
        var options = CliArguments.parse(arguments.toArray(String[]::new)).toOptions();
        assertEquals(Path.of("input.csv").toAbsolutePath(), options.inputFile());
        assertEquals(Path.of("output.txt").toAbsolutePath(), options.outputFile());
        assertTrue(options.crudDemo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"@characters.csv", "name,with,commas.csv", "имя с пробелами.csv"})
    void treatsInputPathAsOneLiteralArgument(String input) {
        var options = CliArguments.parse(new String[]{input, "output.txt"}).toOptions();
        assertEquals(Path.of(input).toAbsolutePath(), options.inputFile());
        assertFalse(options.crudDemo());
    }

    @ParameterizedTest
    @EnumSource(SourceAlias.class)
    void rejectsOutputAliasesWithoutChangingSource(SourceAlias alias) throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("characters.csv"));
        byte[] original = Files.readAllBytes(input);
        Path output = switch (alias) {
            case SAME_PATH -> input;
            case NORMALIZED_PATH -> directory.resolve("./characters.csv");
            case SYMBOLIC_LINK -> Files.createSymbolicLink(directory.resolve("alias.txt"), input);
            case HARD_LINK -> Files.createLink(directory.resolve("alias.txt"), input);
        };
        assertThrows(IllegalArgumentException.class, () -> execute(new String[]{input.toString(), output.toString()}));
        assertArrayEquals(original, Files.readAllBytes(input));
    }

    @Test
    void readFailureDoesNotCreateOutput() {
        Path output = directory.resolve("locations.txt");
        assertThrows(UncheckedIOException.class,
                () -> execute(new String[]{directory.resolve("missing.csv").toString(), output.toString()}));
        assertFalse(Files.exists(output));
    }

    @Test
    void writeFailureDoesNotChangeInput() throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("characters.csv"));
        byte[] original = Files.readAllBytes(input);
        assertThrows(UncheckedIOException.class,
                () -> execute(new String[]{input.toString(), directory.toString()}));
        assertArrayEquals(original, Files.readAllBytes(input));
    }

    private void execute(String[] args) {
        new LabApplication(System.out, System.err).execute(CliArguments.parse(args).toOptions());
    }

    private static Stream<Arguments> locationScenarios() {
        return Stream.of(
                Arguments.of("header only", List.of(), List.of()),
                Arguments.of("all equal", List.of("Zeta", "Zeta", "Zeta"), List.of("Zeta")),
                Arguments.of("interleaved duplicates", List.of("Zeta", "Alpha", "Zeta", "Beta", "Alpha"),
                        List.of("Zeta", "Alpha", "Beta")),
                Arguments.of("unknown and blank", List.of("unknown", "", "unknown", ""), List.of("unknown", ""))
        );
    }

    private enum SourceAlias {
        SAME_PATH, NORMALIZED_PATH, SYMBOLIC_LINK, HARD_LINK
    }
}
