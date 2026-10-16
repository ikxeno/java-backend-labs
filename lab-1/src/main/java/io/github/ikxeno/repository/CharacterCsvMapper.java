package io.github.ikxeno.repository;

import io.github.ikxeno.character.RickAndMortyCharacter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

final class CharacterCsvMapper {
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String STATUS_COLUMN = "status";
    private static final String SPECIES_COLUMN = "species";
    private static final String TYPE_COLUMN = "type";
    private static final String GENDER_COLUMN = "gender";
    private static final String ORIGIN_COLUMN = "origin/name";
    private static final String LOCATION_COLUMN = "location/name";
    private static final String CREATED_COLUMN = "created";
    private static final List<String> HEADERS = List.of(ID_COLUMN, NAME_COLUMN, STATUS_COLUMN, SPECIES_COLUMN,
            TYPE_COLUMN, GENDER_COLUMN, ORIGIN_COLUMN, LOCATION_COLUMN, CREATED_COLUMN);
    private static final CSVFormat READ_FORMAT = CSVFormat.RFC4180.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .get();
    private static final CSVFormat WRITE_FORMAT = CSVFormat.RFC4180.builder()
            .setHeader(HEADERS.toArray(String[]::new))
            .setRecordSeparator("\n")
            .get();

    private CharacterCsvMapper() {
    }

    static CSVFormat readFormat() {
        return READ_FORMAT;
    }

    static CSVFormat writeFormat() {
        return WRITE_FORMAT;
    }

    static void validateHeader(List<String> actualHeaders) {
        if (!actualHeaders.equals(HEADERS)) {
            throw new IllegalArgumentException("Unexpected CSV header: " + actualHeaders);
        }
    }

    static RickAndMortyCharacter parse(CSVRecord record) {
        try {
            if (record.size() != HEADERS.size()) {
                throw new IllegalArgumentException("Expected " + HEADERS.size() + " columns");
            }
            return new RickAndMortyCharacter(
                    Integer.parseInt(record.get(ID_COLUMN)), record.get(NAME_COLUMN), record.get(STATUS_COLUMN),
                    record.get(SPECIES_COLUMN), record.get(TYPE_COLUMN), record.get(GENDER_COLUMN),
                    record.get(ORIGIN_COLUMN), record.get(LOCATION_COLUMN), Instant.parse(record.get(CREATED_COLUMN))
            );
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid character at CSV record " + record.getRecordNumber(), exception);
        }
    }

    static void print(CSVPrinter printer, RickAndMortyCharacter character) throws IOException {
        printer.printRecord(character.id(), character.name(), character.status(), character.species(),
                character.type(), character.gender(), character.originName(), character.locationName(),
                character.created());
    }
}
