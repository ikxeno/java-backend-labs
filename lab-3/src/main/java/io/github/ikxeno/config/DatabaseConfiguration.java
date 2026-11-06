package io.github.ikxeno.config;

import io.github.ikxeno.repository.AirplaneRepository;
import io.github.ikxeno.repository.JdbcAirplaneRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class DatabaseConfiguration {
    @Bean
    AirplaneRepository airplaneRepository(DataSource dataSource) {
        return new JdbcAirplaneRepository(dataSource);
    }
}
