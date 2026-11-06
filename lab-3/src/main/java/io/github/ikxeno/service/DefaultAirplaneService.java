package io.github.ikxeno.service;

import io.github.ikxeno.airplane.AirplaneEntity;
import io.github.ikxeno.repository.AirplaneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public final class DefaultAirplaneService implements AirplaneService {
    private final AirplaneRepository repository;

    public DefaultAirplaneService(AirplaneRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public int save(String model, int seats) {
        return repository.save(new AirplaneEntity(null, model, seats));
    }

    @Override
    public AirplaneEntity findById(int id) {
        validateId(id);
        AirplaneEntity airplane = repository.findById(id);
        if (airplane == null) {
            throw notFound(id);
        }
        return airplane;
    }

    @Override
    public AirplaneEntity findByModel(String model) {
        AirplaneEntity.validateModel(model);
        AirplaneEntity airplane = repository.findByModel(model);
        if (airplane == null) {
            throw new AirplaneNotFoundException(
                    "Airplane with model '" + model + "' was not found");
        }
        return airplane;
    }

    @Override
    public List<AirplaneEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public void update(AirplaneEntity airplane) {
        if (airplane == null || airplane.id() == null) {
            throw new IllegalArgumentException(
                    "An existing airplane must have an ID");
        }
        if (!repository.update(airplane)) {
            throw notFound(airplane.id());
        }
    }

    @Override
    public void deleteById(int id) {
        validateId(id);
        repository.deleteById(id);
    }

    private static void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Airplane ID must be positive");
        }
    }

    private static AirplaneNotFoundException notFound(int id) {
        return new AirplaneNotFoundException(
                "Airplane " + id + " was not found");
    }
}
