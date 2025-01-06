package com.heroku.java.controller;

import com.heroku.java.model.Payment;
import com.heroku.java.model.Booking;
import com.heroku.java.model.Staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

@Controller
public class PaymentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Payment> paymentRowMapper = new RowMapper<>() {
        @Override
        public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Payment pkg = new Payment();
            pkg.setPaymentId(rs.getLong("paymentid"));
            pkg.setBookingId(rs.getLong("bookingid"));
            pkg.setPaymentDate(rs.getTimestamp("paymentdate").toLocalDateTime());
            pkg.setPaymentReceipt(rs.getBytes("paymentreceipt"));

            return pkg;
        }
    };

    @GetMapping("/payment")
    public String showPaymentPage(Model model, HttpSession session) {
        // Fetch the total price from the session or previous request
        Double totalPrice = (Double) session.getAttribute("totalPrice");
        model.addAttribute("totalPrice", totalPrice);
        logger.info("Payment page accessed. TempBooking: {}", session.getAttribute("tempBooking"));
        logger.info("TotalPrice: {}", session.getAttribute("totalPrice"));
        return "payment";
    }

    /*@PostMapping("/submitPayment")
    public String submitPayment(@RequestParam("paymentReceipt") MultipartFile paymentReceipt,
                                HttpSession session) throws IOException {
        Long bookingId = (Long) session.getAttribute("bookingId");
        Double totalPrice = (Double) session.getAttribute("totalPrice");
        if (bookingId == null || totalPrice == null) {
            return "redirect:/createBooking";
        }

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentReceipt(paymentReceipt.getBytes());

        String sql = "INSERT INTO payment (bookingid, paymentdate, paymentreceipt) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, payment.getBookingId(), payment.getPaymentDate(), payment.getPaymentReceipt());
        // jdbcTemplate.execute(sql);

        return "redirect:/custViewBooking";
    }*/
    
    @PostMapping("/submitPayment")
public String submitPayment(@RequestParam("paymentReceipt") MultipartFile paymentReceipt,
                            HttpSession session) throws IOException {
    Booking tempBooking = (Booking) session.getAttribute("tempBooking");
    Double totalPrice = (Double) session.getAttribute("totalPrice");

    if (tempBooking == null || totalPrice == null) {
        return "redirect:/createBooking";
    }

    // Save booking into database
    String insertBookingSql = "INSERT INTO booking (bookingstatus, staffid, custid, packageid, bookingstartdate, bookingenddate) VALUES (?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(insertBookingSql, tempBooking.getBookingStatus(), tempBooking.getStaffId(), tempBooking.getCustId(),
            tempBooking.getPackageId(), tempBooking.getBookingStartDate(), tempBooking.getBookingEndDate());

    // Retrieve the generated booking ID
    String bookingIdSql = "SELECT bookingId FROM booking ORDER BY bookingId DESC LIMIT 1";
    Long bookingId = jdbcTemplate.queryForObject(bookingIdSql, Long.class);

    // Save payment details
    Payment payment = new Payment();
    payment.setBookingId(bookingId);
    payment.setPaymentDate(LocalDateTime.now());
    payment.setPaymentReceipt(paymentReceipt.getBytes());

    String insertPaymentSql = "INSERT INTO payment (bookingid, paymentdate, paymentreceipt) VALUES (?, ?, ?)";
    jdbcTemplate.update(insertPaymentSql, payment.getBookingId(), payment.getPaymentDate(), payment.getPaymentReceipt());

    return "redirect:/custViewBooking";
}

    // Function throws IOException if it fails to read the bytes of the image of the payment receipt
    @GetMapping("/paymentreceipt/{bookingid}")
    public String showPaymentPage(Model model, HttpSession session, @PathVariable Long bookingid) throws IOException {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }
        
        String sql = "SELECT paymentreceipt FROM payment WHERE bookingid = " + bookingid;
        byte[] payment = jdbcTemplate.queryForObject(sql, byte[].class);
        String img = Base64.getEncoder().encodeToString(payment);
        // BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(payment));
        model.addAttribute("receipt_img", img);
        return "paymentreceipt";
    }
}
