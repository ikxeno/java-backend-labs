package io.github.ikxeno.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=")
public final class CliArguments {
    @Parameter(description = "<input.csv> <output.txt>")
    private List<String> files = new ArrayList<>();

    @Parameter(names = {"-i", "--input"}, description = "Input characters CSV", order = 1)
    private String inputFile;

    @Parameter(names = {"-o", "--output"}, description = "Output locations file", order = 2)
    private String outputFile;

    @Parameter(names = "--crud-demo", description = "Demonstrate CRUD in the input CSV", order = 3)
    private boolean crudDemo;

    @Parameter(names = {"-h", "--help"}, help = true, description = "Show this help", order = 4)
    private boolean help;

    public static CliArguments parse(String[] args) {
        var arguments = new CliArguments();
        commander(arguments).parse(args);
        return arguments;
    }

    public static String usage() {
        var text = new StringBuilder();
        commander(new CliArguments()).getUsageFormatter().usage(text);
        return text.toString();
    }

    private static JCommander commander(CliArguments arguments) {
        return JCommander.newBuilder()
                .addObject(arguments)
                .programName("lab-1")
                .expandAtSign(false)
                .build();
    }

    public boolean helpRequested() {
        return help;
    }

    public CommandLineOptions toOptions() {
        if (inputFile != null || outputFile != null) {
            if (!files.isEmpty()) {
                throw new IllegalArgumentException("Use either --input/--output or two positional paths");
            }
            if (inputFile == null || outputFile == null) {
                throw new IllegalArgumentException("Both --input and --output are required");
            }
            return new CommandLineOptions(Path.of(inputFile), Path.of(outputFile), crudDemo);
        }
        if (files.size() != 2) {
            throw new IllegalArgumentException("Expected exactly two paths: input CSV and output text file");
        }
        files.stream()
                .filter(path -> path.startsWith("-"))
                .findFirst()
                .ifPresent(path -> {
                    throw new IllegalArgumentException("Unknown option: " + path
                            + ". Prefix filenames starting with '-' with ./ or use --input/--output");
                });
        return new CommandLineOptions(Path.of(files.get(0)), Path.of(files.get(1)), crudDemo);
    }
}
