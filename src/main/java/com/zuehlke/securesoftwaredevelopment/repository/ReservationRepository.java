package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.City;
import com.zuehlke.securesoftwaredevelopment.domain.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReservationRepository {
    private static final Logger LOG = LoggerFactory.getLogger(ReservationRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(ReservationRepository.class);

    private final DataSource dataSource;
    private final HotelRepository hotelRepository;

    public ReservationRepository(DataSource dataSource, HotelRepository hotelRepository) {
        this.dataSource = dataSource;
        this.hotelRepository = hotelRepository;
    }

    public long create(Reservation r) {
        String query = "INSERT INTO reservation(userId, hotelId, roomTypeId, startDate, endDate, roomsCount, guestsCount, totalPrice) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?," + r.getTotalPrice() + ")";
        long id = -1;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setInt(1, r.getUserId());
            statement.setInt(2, r.getHotelId());
            statement.setInt(3, r.getRoomTypeId());
            statement.setDate(4, java.sql.Date.valueOf(r.getStartDate()));
            statement.setDate(5, java.sql.Date.valueOf(r.getEndDate()));
            statement.setInt(6, r.getRoomsCount());
            statement.setInt(7, r.getGuestsCount());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Creating city failed, no rows affected.");
            }

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getLong(1);
            }
            auditLogger.audit("Kreiran nova rezervacija za hotel ID: " + r.getHotelId() + " od strane korisnika ID: " + r.getUserId());

            LOG.info("Rezervacija uspješno dodan za hotel ID: {} od korisnika ID: {}", r.getHotelId(), r.getUserId());
            return id;
        } catch (SQLException e) {
            LOG.error("Greška prilikom dodavanja rezevacije za hotel ID {}: {}", r.getHotelId(), e.getMessage());
        }

        return id;
    }

    public List<Reservation> getAll() {
        List<Reservation> reservationList = new ArrayList<>();

        String query = "SELECT r.id, r.userId, r.hotelId, h.name, r.roomTypeId, rt.name, r.startDate, r.endDate, r.roomsCount, r.guestsCount, r.totalPrice FROM reservation as r, hotel as h, roomType as rt WHERE r.hotelId = h.id and rt.id = r.roomTypeId";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Reservation r = createPersonFromResultSet(rs);
                reservationList.add(r);
            }
            LOG.info("Učitano {} rezervacija za hotel ID: {}", reservationList.size(), reservationList.get(0).getHotelId());
        } catch (SQLException e) {
            LOG.error("Greška prilikom učitavanja rezervacija za hotel ID {}: {}", reservationList.get(0).getHotelId(), e.getMessage());
        }

        return reservationList;
    }

    public List<Reservation> forUser(Integer userId) {
        List<Reservation> reservationList = new ArrayList<>();

        String query = "SELECT r.id, r.userId, r.hotelId, h.name, r.roomTypeId, rt.name, r.startDate, r.endDate, r.roomsCount, r.guestsCount, r.totalPrice FROM reservation as r, hotel as h, roomType as rt " +
                "WHERE r.hotelId = h.id and rt.id = r.roomTypeId and r.userId = " + userId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Reservation r = createPersonFromResultSet(rs);

                reservationList.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservationList;
    }

    private Reservation createPersonFromResultSet(ResultSet rs) throws java.sql.SQLException {
        Integer id = rs.getInt(1);
        Integer userId = rs.getInt(2);
        Integer hotelId = rs.getInt(3);
        String hotelName = rs.getString(4);
        Integer roomTypeId = rs.getInt(5);
        String roomTypeName = rs.getString(6);
        LocalDate startDate = rs.getDate(7).toLocalDate();
        LocalDate endDate = rs.getDate(8).toLocalDate();
        Integer roomsCount = rs.getInt(9);
        Integer guestCount = rs.getInt(10);
        BigDecimal totalPrice = rs.getBigDecimal(11);

        return new Reservation(
                id,
                userId,
                hotelId,
                hotelName,
                roomTypeId,
                roomTypeName,
                startDate,
                endDate,
                roomsCount,
                guestCount,
                totalPrice
        );
    }

    public void deleteById(Integer id) {
        String query = "DELETE FROM reservation WHERE id = " + id;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

