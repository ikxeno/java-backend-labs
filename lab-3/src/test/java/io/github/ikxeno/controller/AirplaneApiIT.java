package io.github.ikxeno.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AirplaneApiIT {
    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            "postgres:18-alpine")
            .withDatabaseName("airplanes").withUsername("labs")
            .withPassword("labs");

    @Autowired
    private MockMvc mvc;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.hikari.schema", () -> "api_test");
        registry.add("spring.flyway.default-schema", () -> "api_test");
        registry.add("spring.flyway.schemas", () -> "api_test");
    }

    @Test
    void restCrudWorksWithRealPostgresAndFlyway() throws Exception {
        String model = "Airbus " + UUID.randomUUID();
        String request = "{\"model\":\"" + model + "\",\"seats\":180}";
        String location = mvc
                .perform(post("/api/airplanes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated()).andReturn().getResponse()
                .getHeader("Location");
        assertNotNull(location);

        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value(model));
        mvc.perform(get("/api/airplanes/search").param("model", model))
                .andExpect(status().isOk());
        mvc.perform(get("/api/airplanes")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        String update = "{\"model\":\"" + model + "\",\"seats\":194}";
        for (int repeat = 0; repeat < 2; repeat++) {
            mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                    .content(update))
                    .andExpect(status().isNoContent());
        }
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.seats").value(194));

        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content("{\"model\":\"bad\",\"seats\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.seats").value(194));

        for (int repeat = 0; repeat < 2; repeat++) {
            mvc.perform(delete(location)).andExpect(status().isNoContent());
        }
        mvc.perform(get(location)).andExpect(status().isNotFound());
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content(update)).andExpect(status().isNotFound());
        mvc.perform(get("/api/airplanes")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
