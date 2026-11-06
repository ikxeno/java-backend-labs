package io.github.ikxeno.controller;

import io.github.ikxeno.airplane.AirplaneEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AirplaneRequest(
        @NotBlank @Size(max = AirplaneEntity.MAX_MODEL_LENGTH) String model,
        @NotNull @Positive Integer seats) {
}
