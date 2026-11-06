package io.github.ikxeno.repository;

import io.github.ikxeno.airplane.AirplaneEntity;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JdbcAirplaneRepository implements AirplaneRepository {
    private final DataSource dataSource;

    public JdbcAirplaneRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public int save(AirplaneEntity airplane) {
        Objects.requireNonNull(airplane, "airplane");
        if (airplane.id() != null) {
            throw new IllegalArgumentException(
                    "A new airplane must not have an ID");
        }
        String sql = "INSERT INTO airplane (model, seats) "
                + "VALUES (?, ?) RETURNING id";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, airplane.model());
            statement.setInt(2, airplane.seats());
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException(
                            "Insert did not return an airplane ID");
                }
                return resultSet.getInt("id");
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Could not save airplane", exception);
        }
    }

    @Override
    public AirplaneEntity findById(int id) {
        String sql = "SELECT id, model, seats FROM airplane WHERE id = ?";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next() ? readAirplane(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Could not find airplane by ID",
                    exception);
        }
    }

    @Override
    public AirplaneEntity findByModel(String model) {
        AirplaneEntity.validateModel(model);
        String sql = "SELECT id, model, seats FROM airplane "
                + "WHERE model = ? ORDER BY id LIMIT 1";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, model);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next() ? readAirplane(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Could not find airplane by model",
                    exception);
        }
    }

    @Override
    public List<AirplaneEntity> findAll() {
        String sql = "SELECT id, model, seats FROM airplane ORDER BY id";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql);
                var resultSet = statement.executeQuery()) {
            List<AirplaneEntity> airplanes = new ArrayList<>();
            while (resultSet.next()) {
                airplanes.add(readAirplane(resultSet));
            }
            return airplanes;
        } catch (SQLException exception) {
            throw new RepositoryException("Could not list airplanes",
                    exception);
        }
    }

    @Override
    public boolean update(AirplaneEntity airplane) {
        Objects.requireNonNull(airplane, "airplane");
        if (airplane.id() == null) {
            throw new IllegalArgumentException(
                    "An existing airplane must have an ID");
        }
        String sql = "UPDATE airplane SET model = ?, seats = ? WHERE id = ?";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, airplane.model());
            statement.setInt(2, airplane.seats());
            statement.setInt(3, airplane.id());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RepositoryException("Could not update airplane",
                    exception);
        }
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM airplane WHERE id = ?";
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RepositoryException("Could not delete airplane",
                    exception);
        }
    }

    private static AirplaneEntity readAirplane(ResultSet resultSet)
            throws SQLException {
        return new AirplaneEntity(resultSet.getInt("id"),
                resultSet.getString("model"), resultSet.getInt("seats"));
    }
}
