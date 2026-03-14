package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.HashedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class HashedUserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(HashedUserRepository.class);
    private static final AuditLogger auditLogger=AuditLogger.getAuditLogger(HashedUserRepository.class);

    private final DataSource dataSource;

    public HashedUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HashedUser findUser(String username) {
        String sqlQuery = "select passwordHash, salt, totpKey from hashedUsers where username = '" + username + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlQuery)) {
            if (rs.next()) {
                String passwordHash = rs.getString(1);
                String salt = rs.getString(2);
                String totpKey = rs.getString(3);
                LOG.info("Uspješno pronađen korisnik sa korisničkim imenom: {}", username);
                return new HashedUser(username, passwordHash, salt, totpKey);
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom pronalaženja korisnika sa korisničkim imenom {}: {}", username, e.getMessage());
        }
        return null;
    }

    public void saveTotpKey(String username, String totpKey) {
        String sqlQuery = "update hashedUsers set totpKey = ? where username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, totpKey);
            statement.setString(2, username);

            statement.executeUpdate();
            auditLogger.audit("Ažuriran TOTP ključ za korisnika: " + username);
            LOG.info("Uspješno ažuriran TOTP ključ za korisnika: {}", username);


        } catch (SQLException e) {
            LOG.error("Greška prilikom ažuriranja TOTP ključa za korisnika {}: {}", username, e.getMessage());
        }
    }
}
