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
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.stream.Collectors;

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

    /*@GetMapping("/createBooking")
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
        // jdbcTemplate.execute(insertSql);

        // Redirect to payment page with total price
        String packageSql = "SELECT packageprice FROM package WHERE packageid = ?";
        Double totalPrice = jdbcTemplate.queryForObject(packageSql, Double.class, packageId);
        session.setAttribute("totalPrice", totalPrice);
        model.addAttribute("totalPrice", totalPrice);

        // Get bookingId to set it as an attribute of the session
        // String sql = "SELECT bookingId FROM booking WHERE bookingStatus = ? AND custid = ? AND packageid = ? AND bookingstartdate = ? AND bookingenddate = ?";
        String sql = "SELECT bookingId FROM booking ORDER BY bookingId DESC LIMIT 1;";
        Long bookingId = jdbcTemplate.queryForObject(sql, Long.class);
        session.setAttribute("bookingId", bookingId);

        return "redirect:/payment";
    }*/

   @GetMapping("/createBooking")
public String showCreateBookingForm(HttpSession session, Model model) {
    Customer customer = (Customer) session.getAttribute("cust");
    if (customer == null) {
        return "redirect:/custLogin";
    }

    String sql = "SELECT p.*, COUNT(pa.activityid) AS activity_count FROM package p " +
                 "LEFT JOIN packageactivity pa ON p.packageid = pa.packageid " +
                 "GROUP BY p.packageid";

    List<Package> packages = jdbcTemplate.query(sql, (rs, rowNum) -> {
        Package pkg = new Package();
        pkg.setPackageId(rs.getLong("packageid"));
        pkg.setPackageName(rs.getString("packagename"));
        pkg.setPackagePrice(rs.getDouble("packageprice"));
        pkg.setActivityCount(rs.getInt("activity_count"));
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

    // Check if booking duration exceeds 3 days
    if (bookingEndDate.isAfter(bookingStartDate.plusDays(3))) {
        model.addAttribute("dateMessage", "Booking duration cannot exceed 3 days.");
        return "createBooking";
    }

    // Retrieve selected package details
    String packageSql = "SELECT COUNT(activityid) AS activity_count FROM packageactivity WHERE packageid = ?";
    Integer activityCount = jdbcTemplate.queryForObject(packageSql, Integer.class, packageId);

    long bookingDays = java.time.Duration.between(bookingStartDate, bookingEndDate).toDays();

    if ((activityCount >= 8 && bookingDays != 3) || 
        (activityCount <= 3 && bookingDays != 1) || 
        (activityCount >= 4 && activityCount <= 7 && bookingDays != 2)) {
        model.addAttribute("dateMessage", "Selected package duration does not match booking days.");
        return "createBooking";
    }

    // Temporarily store booking details in session (not saved yet)
    Customer customer = (Customer) session.getAttribute("cust");
    Long custId = customer.getCustId();

    // Create the booking object using setter methods
    Booking booking = new Booking();
    booking.setBookingStatus("Pending");
    booking.setStaffId(null); // Assuming staffId is null
    booking.setCustId(custId);
    booking.setPackageId(packageId);
    booking.setBookingStartDate(bookingStartDate);
    booking.setBookingEndDate(bookingEndDate);

    session.setAttribute("tempBooking", booking);

    // Redirect to payment page with total price
    String packagePriceSql = "SELECT packageprice FROM package WHERE packageid = ?";
    Double totalPrice = jdbcTemplate.queryForObject(packagePriceSql, Double.class, packageId);
    session.setAttribute("totalPrice", totalPrice);

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
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "DELETE FROM public.booking WHERE bookingid = ?";
        jdbcTemplate.update(sql, bookingId);
        return "redirect:/staffViewBooking";
    }

    @GetMapping("/checkPackages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPackages(@RequestParam("startDate") String startDateStr,
                                                          @RequestParam("endDate") String endDateStr) {
    try {
        // Parse the date strings
        LocalDateTime startDate = LocalDateTime.parse(startDateStr);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr);
        
        // Check the date duration
        long daysBetween = java.time.Duration.between(startDate, endDate).toDays();
        if (daysBetween > 3) {
            // You can also return an error message here
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Booking not available for more than 3 days"));
        }
        
        // Query available packages based on the date
        String sql = "SELECT p.*, COUNT(pa.activityid) AS activity_count FROM package p " +
                     "LEFT JOIN packageactivity pa ON p.packageid = pa.packageid " +
                     "GROUP BY p.packageid";
        List<Package> packages = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Package pkg = new Package();
            pkg.setPackageId(rs.getLong("packageid"));
            pkg.setPackageName(rs.getString("packagename"));
            pkg.setPackagePrice(rs.getDouble("packageprice"));
            pkg.setActivityCount(rs.getInt("activity_count"));
            return pkg;
        });

        // Filter packages based on activity count and booking days
        packages = packages.stream()
            .filter(pkg -> {
                int activityCount = pkg.getActivityCount();
                long bookingDays = java.time.Duration.between(startDate, endDate).toDays() + 1;
                if ((activityCount >= 8 && bookingDays == 3) || 
                    (activityCount <= 3 && bookingDays == 1) || 
                    (activityCount >= 4 && activityCount <= 7 && bookingDays == 2)) {
                    return true;
                }
                return false;
            })
            .collect(Collectors.toList());
        
        // Return available packages in the response
        Map<String, Object> response = new HashMap<>();
        response.put("packages", packages);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Collections.singletonMap("error", "An error occurred while checking package availability"));
    }
}
}

