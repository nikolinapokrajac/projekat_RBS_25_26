package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersonRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PersonRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(PersonRepository.class);

    private final DataSource dataSource;

    public PersonRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Person> getAll() {
        List<Person> personList = new ArrayList<>();
        String query = "SELECT id, firstName, lastName, email FROM persons";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                personList.add(createPersonFromResultSet(rs));
            }
            auditLogger.audit("Uspješno učitane sve osobe.");
        } catch (SQLException e) {
            LOG.error("Greška prilikom učitavanja osoba: {}", e.getMessage());
        }
        return personList;
    }

    public List<Person> search(String searchTerm) throws SQLException {
        List<Person> personList = new ArrayList<>();
        String query = "SELECT id, firstName, lastName, email FROM persons WHERE UPPER(firstName) like UPPER('%" + searchTerm + "%')" +
                " OR UPPER(lastName) like UPPER('%" + searchTerm + "%')";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                personList.add(createPersonFromResultSet(rs));
            }
            auditLogger.audit("Uspješno izvršena pretraga sa terminom: " + searchTerm);
        }
        return personList;
    }

    public Person get(String personId) {
        String query = "SELECT id, firstName, lastName, email FROM persons WHERE id = " + personId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                return createPersonFromResultSet(rs);
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom učitavanja osobe sa ID {}: {}", personId, e.getMessage());
        }

        return null;
    }

    public void delete(int personId){
        String query = "DELETE FROM persons WHERE id = " + personId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
            auditLogger.audit("Obrisana osoba sa ID: " + personId);
        } catch (SQLException e) {
            LOG.error("Greška prilikom brisanja osobe sa ID {}: {}", personId, e.getMessage());
        }
    }

    private Person createPersonFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String firstName = rs.getString(2);
        String lastName = rs.getString(3);
        String email = rs.getString(4);
        return new Person("" + id, firstName, lastName, email);
    }

    public void update(Person personUpdate) {
        Person personFromDb = get(personUpdate.getId());
        String query = "UPDATE persons SET firstName = ?, lastName = '" + personUpdate.getLastName() + "', email = ? where id = " + personUpdate.getId();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            String firstName = personUpdate.getFirstName() != null ? personUpdate.getFirstName() : personFromDb.getFirstName();
            String email = personUpdate.getEmail() != null ? personUpdate.getEmail() : personFromDb.getEmail();
            statement.setString(1, firstName);
            statement.setString(2, email);
            statement.executeUpdate();
            auditLogger.audit("Ažurirana osoba sa ID: " + personUpdate.getId());
        } catch (SQLException e) {
            LOG.error("Greška prilikom ažuriranja osobe sa ID {}: {}", personUpdate.getId(), e.getMessage());
        }
    }
}
