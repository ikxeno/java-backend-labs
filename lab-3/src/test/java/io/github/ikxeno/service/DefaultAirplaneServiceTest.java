package io.github.ikxeno.service;

import io.github.ikxeno.airplane.AirplaneEntity;
import io.github.ikxeno.repository.AirplaneRepository;
import io.github.ikxeno.repository.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAirplaneServiceTest {
    private static final AirplaneEntity AIRPLANE = new AirplaneEntity(7,
            "Airbus A320", 180);

    @Mock
    private AirplaneRepository repository;

    private AirplaneService service;

    @BeforeEach
    void setUp() {
        service = new DefaultAirplaneService(repository);
    }

    @Test
    void savePassesAnEntityWithoutIdAndReturnsGeneratedId() {
        AirplaneEntity draft = new AirplaneEntity(null, AIRPLANE.model(),
                AIRPLANE.seats());
        when(repository.save(draft)).thenReturn(AIRPLANE.id());

        assertEquals(AIRPLANE.id(), service.save(draft.model(), draft.seats()));
        verify(repository).save(draft);
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource(value = {"NULL,180", "'',180", "'   ',180", "Airbus,0",
            "Airbus,-1"}, nullValues = "NULL")
    void saveRejectsInvalidFieldsBeforeAccessingRepository(String model,
            int seats) {
        assertThrows(IllegalArgumentException.class,
                () -> service.save(model, seats));
        verifyNoInteractions(repository);
    }

    @Test
    void saveAcceptsMaximumModelLengthAndRejectsLongerModel() {
        String model = "A".repeat(AirplaneEntity.MAX_MODEL_LENGTH);
        AirplaneEntity draft = new AirplaneEntity(null, model, 1);
        when(repository.save(draft)).thenReturn(AIRPLANE.id());
        assertEquals(AIRPLANE.id(), service.save(model, 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.save(model + "A", 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.findByModel(model + "A"));
        verify(repository).save(draft);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByIdReturnsStoredAirplane() {
        when(repository.findById(AIRPLANE.id())).thenReturn(AIRPLANE);
        assertEquals(AIRPLANE, service.findById(AIRPLANE.id()));
    }

    @Test
    void findByIdThrowsWhenAbsent() {
        assertThrows(AirplaneNotFoundException.class,
                () -> service.findById(AIRPLANE.id()));
    }

    @Test
    void findByModelReturnsStoredAirplane() {
        when(repository.findByModel(AIRPLANE.model())).thenReturn(AIRPLANE);
        assertEquals(AIRPLANE, service.findByModel(AIRPLANE.model()));
    }

    @Test
    void findByModelThrowsWhenAbsent() {
        assertThrows(AirplaneNotFoundException.class,
                () -> service.findByModel(AIRPLANE.model()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n", "A\0B", "\u0085"})
    void findByModelRejectsInvalidModel(String model) {
        assertThrows(IllegalArgumentException.class,
                () -> service.findByModel(model));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A\0B", "Airbus\nA320", "\u0085"})
    void saveRejectsControlCharactersBeforeAccessingRepository(String model) {
        assertThrows(IllegalArgumentException.class,
                () -> service.save(model, 180));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void findAllReturnsBothEmptyAndPopulatedResults(boolean populated) {
        List<AirplaneEntity> expected = populated
                ? List.of(AIRPLANE)
                : List.of();
        when(repository.findAll()).thenReturn(expected);
        assertEquals(expected, service.findAll());
    }

    @Test
    void updateUsesRepositoryResultWithoutAnExtraRead() {
        when(repository.update(AIRPLANE)).thenReturn(true);
        assertDoesNotThrow(() -> service.update(AIRPLANE));
        verify(repository).update(AIRPLANE);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void updateThrowsWhenAbsent() {
        assertThrows(AirplaneNotFoundException.class,
                () -> service.update(AIRPLANE));
        verify(repository).update(AIRPLANE);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void updateRequiresEntityWithId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(new AirplaneEntity(null, AIRPLANE.model(),
                        AIRPLANE.seats())));
        verifyNoInteractions(repository);
    }

    @Test
    void deleteIsSafeToRepeatWithoutAnExistenceCheck() {
        service.deleteById(AIRPLANE.id());
        service.deleteById(AIRPLANE.id());
        verify(repository, times(2)).deleteById(AIRPLANE.id());
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void readAndDeleteRejectInvalidId(int id) {
        assertThrows(IllegalArgumentException.class,
                () -> service.findById(id));
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteById(id));
        verifyNoInteractions(repository);
    }

    @Test
    void databaseFailureIsNotDisguisedAsAnEmptyResult() {
        RepositoryException failure = new RepositoryException(
                "Connection failed", new SQLException("offline"));
        when(repository.findAll()).thenThrow(failure);
        assertSame(failure,
                assertThrows(RepositoryException.class, service::findAll));
    }
}
