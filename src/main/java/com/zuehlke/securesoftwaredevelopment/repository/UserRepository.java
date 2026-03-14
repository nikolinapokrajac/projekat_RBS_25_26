package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class UserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findUser(String username) {
        String query = "SELECT id, username, password FROM users WHERE username='" + username + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                int id = rs.getInt(1);
                String username1 = rs.getString(2);
                String password = rs.getString(3);
                LOG.info("Korisnik sa korisničkim imenom {} pronađen.", username);
                return new User(id, username1, password);
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom pronalaženja korisnika sa korisničkim imenom {}: {}", username, e.getMessage());
        }
        return null;
    }

    public String findUsername(int id) {
        String query = "SELECT username FROM users WHERE id=" + id;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                String username = rs.getString(1);
                LOG.info("Korisničko ime za korisnika sa ID {} je {}", id, username);
                return username;
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom pronalaženja korisničkog imena za korisnika sa ID {}: {}", id, e.getMessage());
        }
        return null;
    }

    public void updateUsername(int id, String username) {
        String query = "UPDATE users SET username = ? WHERE id = " + id;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setString(1, username);
            statement.executeUpdate();
            LOG.info("Korisničko ime za korisnika sa ID {} uspješno ažurirano na {}", id, username);
        } catch (SQLException e) {
            LOG.error("Greška prilikom ažuriranja korisničkog imena za korisnika sa ID {}: {}", id, e.getMessage());
        }
    }

    public boolean validCredentials(String username, String password) {
        String query = "SELECT username FROM users WHERE username='" + username + "' AND password='" + password + "'";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            boolean valid = rs.next();

            LOG.info("Provjera kredencijala za korisnika {}: {}", username, valid ? "validni" : "nevalidni");

            return valid;

        } catch (SQLException e) {
            LOG.error("Greška prilikom provjere kredencijala za korisnika {}: {}", username, e.getMessage());
        }

        return false;
    }


    public void delete(int userId) {
        String query = "DELETE FROM users WHERE id = " + userId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
            LOG.info("Korisnik sa ID {} uspješno obrisan.", userId);
        } catch (SQLException e) {
            LOG.error("Greška prilikom brisanja korisnika sa ID {}: {}", userId, e.getMessage());
        }
    }
}
