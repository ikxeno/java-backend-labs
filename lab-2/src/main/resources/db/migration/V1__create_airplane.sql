CREATE TABLE airplane (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    model VARCHAR(255) NOT NULL CHECK (model ~ '[^[:space:]]'),
    seats INTEGER NOT NULL CHECK (seats > 0)
);

CREATE INDEX airplane_model_id_idx ON airplane (model, id);
