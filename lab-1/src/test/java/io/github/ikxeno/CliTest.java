package io.github.ikxeno;

import io.github.ikxeno.application.LabApplication;
import io.github.ikxeno.repository.CsvCharacterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliTest {
    private static final Path SOURCE = Path.of("src/main/resources/characters.csv");
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final ByteArrayOutputStream error = new ByteArrayOutputStream();
    private final LabApplication application = new LabApplication(
            new PrintStream(output, true, StandardCharsets.UTF_8),
            new PrintStream(error, true, StandardCharsets.UTF_8));

    @TempDir
    private Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"", "--help", "-h"})
    void printsHelpWithoutOpeningFiles(String argument) {
        assertEquals(0, application.run(argument.isEmpty() ? new String[]{} : new String[]{argument}));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Usage: lab-1"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("--input"));
        assertEquals("", error.toString(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("invalidArguments")
    void reportsArgumentErrorsWithExitCodeTwo(List<String> args) {
        assertEquals(2, application.run(args.toArray(String[]::new)));
        assertTrue(error.toString(StandardCharsets.UTF_8).contains("Usage: lab-1"));
        assertFalse(error.toString(StandardCharsets.UTF_8).contains("Exception"));
        assertEquals("", output.toString(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "long", "equals", "positional"})
    void exportsUsingBothNamedAndPositionalArguments(String form) throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("персонажи, с пробелами.csv"));
        Path destination = directory.resolve("results/locations.txt");
        String[] args = switch (form) {
            case "short" -> new String[]{"-i", input.toString(), "-o", destination.toString()};
            case "long" -> new String[]{"--input", input.toString(), "--output", destination.toString()};
            case "equals" -> new String[]{"--input=" + input, "--output=" + destination};
            default -> new String[]{input.toString(), destination.toString()};
        };
        assertEquals(0, application.run(args));
        assertEquals(8, Files.readAllLines(destination).size());
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Unique locations: 8"));
        assertEquals("", error.toString(StandardCharsets.UTF_8));
    }

    @Test
    void reportsIoFailureWithExitCodeOne() {
        Path destination = directory.resolve("locations.txt");
        assertEquals(1, application.run(new String[]{"--input", "missing.csv", "--output", destination.toString()}));
        assertFalse(Files.exists(destination));
        assertTrue(error.toString(StandardCharsets.UTF_8).contains("Cannot read characters"));
        assertFalse(error.toString(StandardCharsets.UTF_8).contains("Usage:"));
    }

    @Test
    void capturesCrudOutputAlongWithExport() throws IOException {
        Path input = Files.copy(SOURCE, directory.resolve("characters.csv"));
        var before = new CsvCharacterRepository(input).findAll();
        assertEquals(0, application.run(new String[]{"--crud-demo", "-i", input.toString(),
                "-o", directory.resolve("locations.txt").toString()}));
        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Created:"));
        assertTrue(text.contains("Updated:"));
        assertTrue(text.contains("Deleted: true"));
        assertEquals(before, new CsvCharacterRepository(input).findAll());
    }

    private static Stream<List<String>> invalidArguments() {
        return Stream.of(
                List.of("--unknown"), List.of("--unknown", "out.txt"), List.of("--crud-demo"), List.of("only.csv"),
                List.of("--input", "in.csv"), List.of("--output", "out.txt"),
                List.of("--input"), List.of("--input", "in.csv", "--output", "out.txt", "extra.csv"),
                List.of("in.csv", "out.txt", "extra.csv"), List.of("same.csv", "same.csv")
        );
    }
}
