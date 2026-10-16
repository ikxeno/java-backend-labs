package io.github.ikxeno.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record CommandLineOptions(Path inputFile, Path outputFile, boolean crudDemo) {
    public CommandLineOptions {
        inputFile = inputFile.toAbsolutePath().normalize();
        outputFile = outputFile.toAbsolutePath().normalize();
        validateOutputPath(inputFile, outputFile);
    }

    private static void validateOutputPath(Path input, Path output) {
        try {
            if (input.equals(output) || (Files.exists(output) && Files.isSameFile(input, output))) {
                throw new IllegalArgumentException("Input and output paths must differ");
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot compare input and output paths", exception);
        }
    }
}
