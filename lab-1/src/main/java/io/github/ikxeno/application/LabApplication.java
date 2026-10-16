package io.github.ikxeno.application;

import com.beust.jcommander.ParameterException;
import io.github.ikxeno.cli.CliArguments;
import io.github.ikxeno.cli.CommandLineOptions;
import io.github.ikxeno.demo.CrudDemo;
import io.github.ikxeno.io.LocationFileWriter;
import io.github.ikxeno.repository.CsvCharacterRepository;
import io.github.ikxeno.service.LocationService;

import java.io.PrintStream;
import java.io.UncheckedIOException;

public final class LabApplication {
    private static final int SUCCESS = 0;
    private static final int PROCESSING_ERROR = 1;
    private static final int INVALID_ARGUMENTS = 2;

    private final PrintStream output;
    private final PrintStream error;

    public LabApplication(PrintStream output, PrintStream error) {
        this.output = output;
        this.error = error;
    }

    public int run(String[] args) {
        CommandLineOptions options;
        try {
            var arguments = CliArguments.parse(args);
            if (args.length == 0 || arguments.helpRequested()) {
                output.print(CliArguments.usage());
                return SUCCESS;
            }
            options = arguments.toOptions();
        } catch (ParameterException | IllegalArgumentException exception) {
            error.println("Error: " + exception.getMessage());
            error.print(CliArguments.usage());
            return INVALID_ARGUMENTS;
        } catch (UncheckedIOException exception) {
            error.println("Error: " + exception.getMessage());
            return PROCESSING_ERROR;
        }

        try {
            execute(options);
            return SUCCESS;
        } catch (RuntimeException exception) {
            error.println("Error: " + exception.getMessage());
            return PROCESSING_ERROR;
        }
    }

    public void execute(CommandLineOptions options) {
        var repository = new CsvCharacterRepository(options.inputFile());
        var locations = new LocationService(repository).findUniqueLocations();
        new LocationFileWriter().write(options.outputFile(), locations);
        output.println("Unique locations: " + locations.size());
        locations.forEach(output::println);
        output.println("Saved to: " + options.outputFile());

        if (options.crudDemo()) {
            new CrudDemo(repository, output).run();
        }
    }
}
