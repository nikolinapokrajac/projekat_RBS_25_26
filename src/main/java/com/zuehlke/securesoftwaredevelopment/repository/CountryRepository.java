package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CountryRepository {
    private static final Logger LOG = LoggerFactory.getLogger(CountryRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(CountryRepository.class);
    private final DataSource dataSource;

    public CountryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Country> getAll() {
        List<Country> countryList = new ArrayList<>();
        String query = "SELECT c.id, c.name FROM country as c";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                countryList.add(new Country(id, name));
                LOG.info("Nova država učitana sa ID: {}", id);
            }
            return countryList;
        } catch (SQLException e) {
            LOG.error("Greška prilikom učitavanja države: {}", e.getMessage());
        }

        return null;
    }

    public Country findById(Integer countryId) {
        String query = "SELECT c.id, c.name FROM country as c WHERE c.id = " + countryId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                LOG.info("Država sa ID {} učitana", id);
                return new Country(id, name);
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom učitavanja države sa ID {}: {}", countryId, e.getMessage());
        }

        return null;
    }

    public List<Country> findByName(String name) {
        String query = "SELECT c.id FROM country as c WHERE c.name like '" + name + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            List<Country> countryList = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt(1);
                countryList.add(new Country(id, name));
                LOG.info("Pronađena država: {}", rs.getString("name"));
            }

            return countryList;
        } catch (SQLException e) {
            LOG.error("Greška prilikom pretrage država: {}", e.getMessage());
        }

        return null;
    }

    public long create(Country country) {
        String query = "INSERT INTO country(name) VALUES('" + country.getName() + "')";
        long id = 0;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            int rows = statement.executeUpdate(query);

            if (rows == 0) {
                throw new SQLException("Creating city failed, no rows affected.");
            }
            auditLogger.audit("Kreirana nova država sa ID: " + id + " sa nazivom: " + country.getName());
            LOG.info("Nova država sa ID {} i nazivom '{}' uspješno kreirana.", id, country.getName());
        } catch (SQLException e) {
            LOG.error("Greška prilikom kreiranja države: {}", e.getMessage());
        }

        return id;
    }
}
