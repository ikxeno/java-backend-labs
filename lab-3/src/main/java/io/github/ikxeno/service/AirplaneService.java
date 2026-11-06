package io.github.ikxeno.service;

import io.github.ikxeno.airplane.AirplaneEntity;

import java.util.List;

/**
 * Lookups by ID or model and updates throw AirplaneNotFoundException when the
 * airplane is absent.
 */
public interface AirplaneService {
    int save(String model, int seats);

    AirplaneEntity findById(int id);

    AirplaneEntity findByModel(String model);

    List<AirplaneEntity> findAll();

    void update(AirplaneEntity airplane);

    /** Deleting an absent airplane has no effect. */
    void deleteById(int id);
}
