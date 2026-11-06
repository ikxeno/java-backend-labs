package io.github.ikxeno.controller;

import io.github.ikxeno.airplane.AirplaneEntity;
import io.github.ikxeno.repository.AirplaneRepository;
import io.github.ikxeno.repository.RepositoryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration")
@AutoConfigureMockMvc
class AirplaneControllerTest {
    private static final String BASE_PATH = "/api/airplanes";
    private static final AirplaneEntity AIRPLANE = new AirplaneEntity(7,
            "Airbus A320", 180);
    private static final String REQUEST = """
            {"model":"Airbus A320","seats":180}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AirplaneRepository repository;

    @Test
    void createReturnsIdAndLocation() throws Exception {
        when(repository.save(
                new AirplaneEntity(null, AIRPLANE.model(), AIRPLANE.seats())))
                .thenReturn(AIRPLANE.id());

        mvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        BASE_PATH + "/" + AIRPLANE.id()))
                .andExpect(jsonPath("$.id").value(AIRPLANE.id()));
    }

    @Test
    void readByIdReturnsAirplane() throws Exception {
        when(repository.findById(AIRPLANE.id())).thenReturn(AIRPLANE);

        mvc.perform(get(BASE_PATH + "/{id}", AIRPLANE.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(AIRPLANE.id()))
                .andExpect(jsonPath("$.model").value(AIRPLANE.model()))
                .andExpect(jsonPath("$.seats").value(AIRPLANE.seats()));
    }

    @Test
    void searchUsesModelQueryParameter() throws Exception {
        when(repository.findByModel(AIRPLANE.model())).thenReturn(AIRPLANE);

        mvc.perform(get(BASE_PATH + "/search").param("model", AIRPLANE.model()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(AIRPLANE.id()));
    }

    @Test
    void listReturnsJsonArray() throws Exception {
        when(repository.findAll()).thenReturn(List.of(AIRPLANE));
        mvc.perform(get(BASE_PATH)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(AIRPLANE.id()));
    }

    @Test
    void updateUsesPathIdAndReturnsNoContent() throws Exception {
        when(repository.update(AIRPLANE)).thenReturn(true);

        mvc.perform(put(BASE_PATH + "/{id}", AIRPLANE.id())
                .contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(repository).update(AIRPLANE);
    }

    @Test
    void deleteReturnsNoContentEvenWhenAbsent() throws Exception {
        mvc.perform(delete(BASE_PATH + "/{id}", AIRPLANE.id()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(repository).deleteById(AIRPLANE.id());
    }

    @Test
    void missingReadsAndUpdateReturnNotFound() throws Exception {
        mvc.perform(get(BASE_PATH + "/{id}", AIRPLANE.id()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
        mvc.perform(get(BASE_PATH + "/search").param("model", AIRPLANE.model()))
                .andExpect(status().isNotFound());
        mvc.perform(put(BASE_PATH + "/{id}", AIRPLANE.id())
                .contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{", "{\"model\":\"\",\"seats\":180}",
            "{\"model\":\"Airbus\",\"seats\":0}",
            "{\"model\":\"Airbus\",\"seats\":-1}",
            "{\"model\":\"Airbus\",\"seats\":null}",
            "{\"model\":\"Airbus\",\"seats\":1.5}",
            "{\"model\":\"Airbus\",\"seats\":2147483648}"})
    void invalidBodiesAreRejectedBeforeRepository(String body)
            throws Exception {
        mvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
        mvc.perform(put(BASE_PATH + "/{id}", AIRPLANE.id())
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "text", "2147483648"})
    void invalidPathIdsAreRejected(String id) throws Exception {
        mvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isBadRequest());
        mvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void searchRequiresNonBlankModel() throws Exception {
        mvc.perform(get(BASE_PATH + "/search"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(BASE_PATH + "/search").param("model", " "))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void oversizedModelIsRejectedBeforeRepository() throws Exception {
        String body = "{\"model\":\""
                + "A".repeat(AirplaneEntity.MAX_MODEL_LENGTH + 1)
                + "\",\"seats\":180}";
        mvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\\u0000", "\\u0085", "\\n"})
    void controlCharactersInJsonAreRejectedBeforeRepository(
            String escapedControl) throws Exception {
        String body = "{\"model\":\"Airbus" + escapedControl
                + "A320\",\"seats\":180}";
        mvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
        mvc.perform(put(BASE_PATH + "/{id}", AIRPLANE.id())
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A\0B", "\u0085"})
    void searchRejectsControlCharacters(String model) throws Exception {
        mvc.perform(get(BASE_PATH + "/search").param("model", model))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void databaseErrorsDoNotExposeSqlDetails() throws Exception {
        when(repository.findAll()).thenThrow(new RepositoryException(
                "private SQL", new SQLException("credentials")));
        mvc.perform(get(BASE_PATH)).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail")
                        .value("Database operation failed"));
    }
}
