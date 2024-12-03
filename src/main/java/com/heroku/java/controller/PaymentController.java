package com.heroku.java.controller;

import com.heroku.java.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

@Controller
public class PaymentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/payment")
    public String showPaymentPage(Model model, HttpSession session) {
        // Fetch the total price from the session or previous request
        Double totalPrice = (Double) session.getAttribute("totalPrice");
        model.addAttribute("totalPrice", totalPrice);
        return "payment";
    }

    @PostMapping("/submitPayment")
public String submitPayment(@RequestParam("paymentReceipt") MultipartFile paymentReceipt,
                            HttpSession session) throws IOException {
    Long bookingId = (Long) session.getAttribute("bookingId");
    if (bookingId == null) {
        return "redirect:/custViewBooking";
    }

    Payment payment = new Payment();
    payment.setBookingId(bookingId);
    payment.setPaymentDate(LocalDateTime.now());
    payment.setPaymentReceipt(paymentReceipt.getBytes());

    String sql = "INSERT INTO payment (bookingid, paymentdate, paymentreceipt) VALUES (?, ?, ?)";
    jdbcTemplate.update(sql, payment.getBookingId(), payment.getPaymentDate(), payment.getPaymentReceipt());

    return "redirect:/custViewBooking";
}
}
