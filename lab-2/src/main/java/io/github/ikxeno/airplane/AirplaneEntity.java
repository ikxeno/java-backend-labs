package io.github.ikxeno.airplane;

public record AirplaneEntity(Integer id, String model, int seats) {
    public static final int MAX_MODEL_LENGTH = 255;

    public AirplaneEntity {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Airplane ID must be positive");
        }
        validateModel(model);
        if (seats <= 0) {
            throw new IllegalArgumentException(
                    "Number of seats must be positive");
        }
    }

    public static void validateModel(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException(
                    "Airplane model must not be blank");
        }
        if (model.length() > MAX_MODEL_LENGTH) {
            throw new IllegalArgumentException("Airplane model must not exceed "
                    + MAX_MODEL_LENGTH + " characters");
        }
        if (model.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "Airplane model must not contain control characters");
        }
    }
}
