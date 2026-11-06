package io.github.ikxeno.repository;

import io.github.ikxeno.airplane.AirplaneEntity;

import java.util.List;

/** All operations except save are idempotent. */
public interface AirplaneRepository {
    /** Stores an airplane without an ID and returns the generated ID. */
    int save(AirplaneEntity airplane);

    /** Returns null when the airplane is absent. */
    AirplaneEntity findById(int id);

    /**
     * Returns the exact model match with the smallest ID, or null when absent.
     */
    AirplaneEntity findByModel(String model);

    /** Returns airplanes in ascending ID order. */
    List<AirplaneEntity> findAll();

    /**
     * Replaces fields of the given ID; returns false when the airplane is
     * absent.
     */
    boolean update(AirplaneEntity airplane);

    /** Deleting an absent airplane has no effect. */
    void deleteById(int id);
}
