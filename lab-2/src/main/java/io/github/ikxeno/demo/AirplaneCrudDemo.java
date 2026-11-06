package io.github.ikxeno.demo;

import io.github.ikxeno.airplane.AirplaneEntity;
import io.github.ikxeno.repository.AirplaneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AirplaneCrudDemo {
    private static final Logger LOG = LoggerFactory
            .getLogger(AirplaneCrudDemo.class);

    private final AirplaneRepository repository;

    public AirplaneCrudDemo(AirplaneRepository repository) {
        this.repository = repository;
    }

    public void run() {
        AirplaneEntity draft = new AirplaneEntity(null, "Airbus A320", 180);
        int id = repository.save(draft);
        LOG.info("Created airplane: {}", id);
        LOG.info("Find by ID: {}", repository.findById(id));
        LOG.info("Find by model: {}", repository.findByModel(draft.model()));
        LOG.info("All airplanes: {}", repository.findAll());
        AirplaneEntity updated = new AirplaneEntity(id, "Airbus A320neo", 194);
        LOG.info("Updated: {}", repository.update(updated));
        LOG.info("Repeated update: {}", repository.update(updated));
        LOG.info("After update: {}", repository.findById(id));
        repository.deleteById(id);
        repository.deleteById(id);
        LOG.info("After repeated delete: {}", repository.findById(id));
    }
}
