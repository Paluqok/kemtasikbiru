package com.heroku.java.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.heroku.java.model.Booking;
import com.heroku.java.model.Staff;

import jakarta.servlet.http.HttpSession;

@RestController
public class StaffBookingApiController {

    private final DataSource dataSource;

    @Autowired
    public StaffBookingApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/staff/bookings")
    public ResponseEntity<?> getStaffBookings() {
      

        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT b.bookingid, b.bookingstartdate, b.bookingenddate, b.bookingstatus, p.packagename, p.packageprice "
                    + "FROM public.booking b JOIN public.package p ON b.packageid = p.packageid";
            try (PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Booking booking = new Booking();
                    booking.setBookingId(resultSet.getLong("bookingid"));
                    booking.setBookingStartDate(resultSet.getTimestamp("bookingstartdate").toLocalDateTime());
                    booking.setBookingEndDate(resultSet.getTimestamp("bookingenddate").toLocalDateTime());
                    booking.setBookingStatus(resultSet.getString("bookingstatus"));
                    booking.setPackageName(resultSet.getString("packagename"));
                    booking.setPackagePrice(resultSet.getDouble("packageprice"));

                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving bookings");
        }

        return ResponseEntity.ok(bookings);
    }
}
