package com.heroku.java.model;

import java.time.LocalDateTime;

public class Payment {
    private Long paymentId;
    private Long bookingId;
    private LocalDateTime paymentDate;
    private byte[] paymentReceipt;

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public byte[] getPaymentReceipt() {
        return paymentReceipt;
    }

    public void setPaymentReceipt(byte[] paymentReceipt) {
        this.paymentReceipt = paymentReceipt;
    }
}
