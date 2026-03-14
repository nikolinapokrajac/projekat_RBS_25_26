package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RatingRepository {

    private static final Logger LOG = LoggerFactory.getLogger(RatingRepository.class);

    private final DataSource dataSource;

    public RatingRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createOrUpdate(Rating rating) {
        String query = "SELECT hotelId, userId, rating FROM ratings WHERE hotelId = " + rating.getHotelId() + " AND userID = " + rating.getUserId();
        String query2 = "update ratings SET rating = ? WHERE hotelId = ? AND userId = ?";
        String query3 = "insert into ratings(hotelId, userId, rating) values (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)
        ) {
            if (rs.next()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query2);
                preparedStatement.setInt(1, rating.getRating());
                preparedStatement.setInt(2, rating.getHotelId());
                preparedStatement.setInt(3, rating.getUserId());
                preparedStatement.executeUpdate();
                LOG.info("Ažurirana ocjena za hotel ID {} od korisnika ID {}", rating.getHotelId(), rating.getUserId());
            } else {
                PreparedStatement preparedStatement = connection.prepareStatement(query3);
                preparedStatement.setInt(1, rating.getHotelId());
                preparedStatement.setInt(2, rating.getUserId());
                preparedStatement.setInt(3, rating.getRating());
                preparedStatement.executeUpdate();
                LOG.info("Dodana nova ocjena za hotel ID {} od korisnika ID {}", rating.getHotelId(), rating.getUserId());
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom kreiranja ili ažuriranja ocjene za hotel ID {} od korisnika ID {}: {}", rating.getHotelId(), rating.getUserId(), e.getMessage());
        }
    }

    public List<Rating> getAll(String hotelId) {
        List<Rating> ratingList = new ArrayList<>();
        String query = "SELECT hotelId, userId, rating FROM ratings WHERE hotelId = " + hotelId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                ratingList.add(new Rating(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
                LOG.info("Uspješno učitane ocjene za hotel ID {}", hotelId);
            }
        } catch (SQLException e) {
            LOG.error("Greška prilikom učitavanja ocjena za hotel ID {}: {}", hotelId, e.getMessage());
        }
        return ratingList;
    }
}
