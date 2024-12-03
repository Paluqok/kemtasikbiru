package com.heroku.java.controller;

import com.heroku.java.model.Booking;
import com.heroku.java.model.Package;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

@Controller
public class BookingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
     @Autowired
    public BookingController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private final RowMapper<Booking> bookingRowMapper = new RowMapper<>() {
        @Override
        public Booking mapRow(ResultSet rs, int rowNum) throws SQLException {
            Booking booking = new Booking();
            booking.setBookingId(rs.getLong("bookingid"));
            booking.setBookingStatus(rs.getString("bookingstatus"));
            booking.setStaffId(rs.getLong("staffid"));
            booking.setCustId(rs.getLong("custid"));
            booking.setPackageId(rs.getLong("packageid"));
            booking.setBookingStartDate(rs.getTimestamp("bookingstartdate").toLocalDateTime());
            booking.setBookingEndDate(rs.getTimestamp("bookingenddate").toLocalDateTime());
            return booking;
        }
    };

    @GetMapping("/createBooking")
    public String showCreateBookingForm(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("cust");
        if (customer == null) {
            return "redirect:/custLogin";
        }

        String sql = "SELECT * FROM package";
        List<Package> packages = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Package pkg = new Package();
            pkg.setPackageId(rs.getLong("packageid"));
            pkg.setPackageName(rs.getString("packagename"));
            pkg.setPackagePrice(rs.getDouble("packageprice"));
            return pkg;
        });
        model.addAttribute("packages", packages);
        return "createBooking";
    }

    @PostMapping("/createBooking")
    public String createBooking(@RequestParam("bookingStartDate") LocalDateTime bookingStartDate,
                                @RequestParam("bookingEndDate") LocalDateTime bookingEndDate,
                                @RequestParam("packageId") Long packageId,
                                HttpSession session, Model model) {
        // Check for date clash
        String clashCheckSql = "SELECT COUNT(*) FROM booking WHERE " +
                "((bookingstartdate <= ? AND bookingenddate >= ?) OR " +
                "(bookingstartdate <= ? AND bookingenddate >= ?) OR " +
                "(bookingstartdate >= ? AND bookingenddate <= ?))";
        int clashCount = jdbcTemplate.queryForObject(clashCheckSql, Integer.class, 
                bookingStartDate, bookingStartDate, 
                bookingEndDate, bookingEndDate, 
                bookingStartDate, bookingEndDate);
        if (clashCount >= 5) {
            model.addAttribute("dateMessage", "Booking is currently full at that date.");
            return "createBooking";
        }

        // Check if booking duration is more than 3 days
        if (bookingEndDate.isAfter(bookingStartDate.plusDays(3))) {
            model.addAttribute("dateMessage", "Not available for more than 3 days.");
            return "createBooking";
        }

        // Get the customer ID from session
        Customer customer = (Customer) session.getAttribute("cust");
        Long custId = customer.getCustId();

        // Insert booking into database
        String insertSql = "INSERT INTO booking (bookingstatus, staffid, custid, packageid, bookingstartdate, bookingenddate) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, "Pending", null, custId, packageId, bookingStartDate, bookingEndDate);

        // Redirect to payment page with total price
        String packageSql = "SELECT packageprice FROM package WHERE packageid = ?";
        Double totalPrice = jdbcTemplate.queryForObject(packageSql, Double.class, packageId);
        model.addAttribute("totalPrice", totalPrice);

        return "redirect:/payment";
    }

    @GetMapping("/custViewBooking")
    public String customerViewBooking(HttpSession session, Model model) {
        Long custid = (Long) session.getAttribute("custid");

        List<Booking> bookings = new ArrayList<>();
        try{
            Connection conn = dataSource.getConnection();
            String sql = "SELECT b.bookingstartdate,b.bookingenddate,b.bookingstatus,p.packagename,p.packageprice"
            +" FROM public.booking b JOIN public.package p ON b.packageid = p.packageid WHERE custid=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, custid);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                Booking booking = new Booking();
                booking.setBookingStartDate(resultSet.getTimestamp("bookingstartdate").toLocalDateTime());
                booking.setBookingEndDate(resultSet.getTimestamp("bookingenddate").toLocalDateTime());
                booking.setBookingStatus(resultSet.getString("bookingstatus"));
                booking.setPackageName(resultSet.getString("packagename"));
                booking.setPackagePrice(resultSet.getDouble("packageprice"));

                bookings.add(booking);
            }

            model.addAttribute("bookings", bookings);

        }catch(SQLException e){
            e.printStackTrace();
        }
        return "custViewBooking";
    }

    @GetMapping("/staffViewBooking")
    public String staffViewBooking(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }
        List<Booking> bookings = new ArrayList<>();
        try{
            Connection conn = dataSource.getConnection();
            String sql = "SELECT b.bookingid,b.bookingstartdate,b.bookingenddate,b.bookingstatus,p.packagename,p.packageprice"
            +" FROM public.booking b JOIN public.package p ON b.packageid = p.packageid";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                Booking booking = new Booking();
                booking.setBookingId(resultSet.getLong("bookingid"));
                booking.setBookingStartDate(resultSet.getTimestamp("bookingstartdate").toLocalDateTime());
                booking.setBookingEndDate(resultSet.getTimestamp("bookingenddate").toLocalDateTime());
                booking.setBookingStatus(resultSet.getString("bookingstatus"));
                booking.setPackageName(resultSet.getString("packagename"));
                booking.setPackagePrice(resultSet.getDouble("packageprice"));

                bookings.add(booking);
            }

            model.addAttribute("bookings", bookings);

        }catch(SQLException e){
            e.printStackTrace();
        }
        return "staffViewBooking";
    }

    @PostMapping("/approveBooking/{id}")
    public String approveBooking(@PathVariable("id") Long bookingId, HttpSession session) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }
        Long staffId = staff.getStaffId();

        String sql = "UPDATE booking SET bookingstatus = 'Approved', staffid = ? WHERE bookingid = ?";
        jdbcTemplate.update(sql, staffId, bookingId);
        return "redirect:/staffViewBooking";
    }

    @PostMapping("/rejectBooking/{id}")
    public String rejectBooking(@PathVariable("id") Long bookingId, HttpSession session) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "UPDATE booking SET bookingstatus = 'Rejected' WHERE bookingid = ?";
        jdbcTemplate.update(sql, bookingId);
        return "redirect:/staffViewBooking";
    }
    
    @PostMapping("/deleteBooking/{id}")
    public String deleteBooking(@PathVariable("id") Long bookingId, HttpSession session) {
        Long custid = (Long) session.getAttribute("custid");
        if (custid == null) {
            return "redirect:/custLogin";
        }

        String sql = "DELETE FROM public.booking WHERE bookingid = ?";
        jdbcTemplate.update(sql, bookingId);
        return "redirect:/custViewBooking";
    }
}

