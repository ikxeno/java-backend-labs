package io.github.ikxeno;

import io.github.ikxeno.config.DatabaseConfiguration;
import io.github.ikxeno.demo.AirplaneCrudDemo;
import io.github.ikxeno.repository.JdbcAirplaneRepository;

import java.io.IOException;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        try (var dataSource = DatabaseConfiguration.openDataSource()) {
            DatabaseConfiguration.migrate(dataSource);
            new AirplaneCrudDemo(new JdbcAirplaneRepository(dataSource)).run();
        }
    }
}
