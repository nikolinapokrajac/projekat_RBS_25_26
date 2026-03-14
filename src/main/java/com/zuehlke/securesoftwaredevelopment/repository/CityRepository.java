package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CityRepository {
    private static final Logger LOG = LoggerFactory.getLogger(CityRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(CityRepository.class);
    private final DataSource dataSource;

    public CityRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<City> getAll() {
        List<City> cityList = new ArrayList<>();
        String query = "SELECT c.id, c.countryId, c.name, ct.name FROM city as c, country as ct WHERE ct.id = c.countryId";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt(1);
                int countryId = rs.getInt(2);
                String name = rs.getString(3);
                String countryName = rs.getString(4);

                cityList.add(new City(id, countryId, name, countryName));
                LOG.info("Novi grad ucitan sa ID: {}", id);
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom učitavanja grada: {}", e.getMessage());
        }

        return cityList;
    }

    public City findById(Integer cityId) {
        String query = "SELECT c.id, c.countryId, c.name FROM city as c WHERE c.id = " + cityId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                int id = rs.getInt(1);
                int countryId = rs.getInt(2);
                String name = rs.getString(3);
                LOG.info("Grad sa ID {} učitan", cityId);
                return new City(id, countryId, name);
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom učitavanja grada sa ID {}: {}", cityId, e.getMessage());
        }

        return null;
    }

    public List<City> findByName(String name) {
        String query = "SELECT c.id, c.countryId, c.name FROM city c WHERE c.name LIKE ?";


        List<City> cityList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + name + "%");

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int countryId = rs.getInt("countryId");
                    String cityName = rs.getString("name");

                    cityList.add(new City(id, countryId, cityName));
                    LOG.info("Pronađen grad: {}", rs.getString("name"));
                }

            }

        } catch (SQLException e) {
            LOG.error("Greška prilikom pretrage gradova: {}", e.getMessage());
        }

        return cityList;
    }

    public long create(City city) {
        String query = "INSERT INTO city(countryid, name) VALUES(?, ?)";
        long id = -1;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ) {
            statement.setInt(1, city.getCountryId());
            statement.setString(2, city.getName());
            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Creating city failed, no rows affected.");
            }

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getLong(1);
            }
            auditLogger.audit("Kreiran novi grad sa ID: " + id + " sa nazivom: " + city.getName());
            LOG.info("Novi grad sa ID {} i nazivom '{}' uspješno kreiran.", id, city.getName());


            return id;
        } catch (SQLException e) {
            LOG.error("Greška prilikom kreiranja grada: {}", e.getMessage());
        }

        return id;
    }
}
