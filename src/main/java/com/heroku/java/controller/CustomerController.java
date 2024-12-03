package com.heroku.java.controller;

import java.io.IOException;
import java.sql.*;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.heroku.java.model.Customer;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;

@Controller
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final DataSource dataSource;

    @Autowired
    public CustomerController(DataSource dataSource) {
        this.dataSource = dataSource;
    }
     
    @GetMapping("/custSignUp")
    public String createCustomerAccount(){
        return "customer/custSignUp";
    }

    @PostMapping("/createAccountCustomer")
    public String createAccountCustomer(@RequestParam("custName") String custName,
                                   @RequestParam("custEmail") String custEmail,
                                   @RequestParam("custAddress") String custAddress,
                                   @RequestParam("custPhoneNo") String custPhoneNo,
                                   @RequestParam("custPassword") String custPassword) throws IOException {
        
        Customer cust = new Customer();
        cust.setCustName(custName);
        cust.setCustEmail(custEmail);
        cust.setCustAddress(custAddress);
        cust.setCustPhoneNo(custPhoneNo);
        cust.setCustPassword(custPassword);

        try (Connection connection = dataSource.getConnection()) {

            System.out.println("Received cust details:");
            System.out.println("Name: " + cust.getCustName());
            System.out.println("Email: " + cust.getCustEmail());
            System.out.println("Address: " + cust.getCustAddress());
            System.out.println("Phone No: " + cust.getCustPhoneNo());
            System.out.println("Password: " + cust.getCustPassword());

            String custSql = "INSERT INTO public.customer(custname, custemail, custaddress, custphoneno, custpassword) VALUES (?, ?, ?, ?, ?) RETURNING custid";

            try (PreparedStatement statement = connection.prepareStatement(custSql)) {
                statement.setString(1, cust.getCustName());
                statement.setString(2, cust.getCustEmail());
                statement.setString(3, cust.getCustAddress());
                statement.setString(4, cust.getCustPhoneNo());
                statement.setString(5, cust.getCustPassword()); // Set the Base64-encoded image

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Long custId = resultSet.getLong("custid");
                        cust.setCustId(custId);
                        logger.info("Inserted customer with ID: {}", custId);
                    } else {
                        throw new SQLException("Failed to insert cust, no ID obtained.");
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
            // Handle exception appropriately, e.g., log error or rethrow as runtime exception
            throw new RuntimeException("Failed to insert cust", e);
        }

        return "redirect:/custLogin"; // Return the created cust object
    }

    @GetMapping("/custLogin")
    public String custLogin() {
        return "customer/custLogin";
    }

    @PostMapping("/loginCustomer")
    public String custLogins(HttpSession session,
                              @RequestParam("custEmail") String custEmail,
                              @RequestParam("custPassword") String custPassword) throws LoginException {

        logger.info("Attempting to log in cust with email: {}", custEmail);

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT custid, custname, custemail, custphoneno, custaddress, custpassword FROM public.customer WHERE custemail = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, custEmail);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Customer cust = new Customer();
                        cust.setCustId(resultSet.getLong("custid"));
                        cust.setCustName(resultSet.getString("custname"));
                        cust.setCustEmail(resultSet.getString("custemail"));
                        cust.setCustPhoneNo(resultSet.getString("custphoneno"));
                        cust.setCustAddress(resultSet.getString("custaddress"));
                        cust.setCustPassword(resultSet.getString("custpassword"));

                        logger.info("Customer found: {}", cust.getCustName());

                        if (cust.getCustPassword().equals(custPassword)) {
                            logger.info("Password matches for cust: {}", cust.getCustName());
                            session.setAttribute("custname", cust.getCustName());
                            session.setAttribute("custid", cust.getCustId());
                            session.setAttribute("cust", cust);
                            return "redirect:/homeCustomer";
                        } else {
                            logger.warn("Password does not match for cust: {}", cust.getCustName());
                        }
                    } else {
                        logger.warn("No cust found with email: {}", custEmail);
                    }
                }
            }
            return "redirect:/custLogin";
        } catch (SQLException e) {
            logger.error("Error during cust login", e);
            return "redirect:/error";
        }
    }

    @GetMapping("/homeCustomer")
    public String homeCustomer(HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("custid");
        if (custId == null) {
            logger.warn("custId is null in session");
            return "redirect:/custLogin";
        }

        Customer cust = (Customer) session.getAttribute("cust");
        if (cust == null) {
            logger.warn("cust is null in session");
            return "redirect:/custLogin";
        }

        model.addAttribute("cust", cust);
        return "homeCustomer";
    }

    @GetMapping("/custProfile")
    public String custProfile(HttpSession session, Model model) {
        Customer cust = (Customer) session.getAttribute("cust");
        if (cust == null) {
            return "redirect:/custLogin";
        }
        model.addAttribute("customer", cust);
        return "customer/custProfile";
    }

    // Serve the update form with current data
@GetMapping("/custUpdate")
public String showUpdateForm(HttpSession session, Model model) {
    Customer cust = (Customer) session.getAttribute("cust");
    if (cust == null) {
        return "redirect:/custLogin";
    }
    model.addAttribute("customer", cust);
    return "customer/custUpdate";
}

// Process the update form
@PostMapping("/updateCustomer")
public String updateCustomer(
    @RequestParam("custId") Long custId,
    @RequestParam("custName") String custName,
    @RequestParam("custEmail") String custEmail,
    @RequestParam("custAddress") String custAddress,
    @RequestParam("custPhoneNo") String custPhoneNo,
    @RequestParam(value = "custPassword", required = false) String custPassword,
    @RequestParam(value = "checkPassword", required = false) String checkPassword,
    HttpSession session) throws IOException {

    // Fetch the current cust object from the session
    Customer currentCustomer = (Customer) session.getAttribute("cust");
    if (currentCustomer == null) {
        return "redirect:/custLogin";
    }

    // Check that the custId matches the ID of the cust in the session
    if (!custId.equals(currentCustomer.getCustId())) {
        return "redirect:/custProfile";
    }

    // Update the cust details
    currentCustomer.setCustName(custName);
    currentCustomer.setCustEmail(custEmail);
    currentCustomer.setCustAddress(custAddress);
    currentCustomer.setCustPhoneNo(custPhoneNo);

    // Update password only if provided and passwords match
    if (custPassword != null && !custPassword.isEmpty()) {
        if (custPassword.equals(checkPassword)) {
            currentCustomer.setCustPassword(custPassword);
        } else {
            // Add an error message to the model if passwords do not match
            return "redirect:/custUpdate?error=true";
        }
    }

    // Save the updated cust object
    try (Connection connection = dataSource.getConnection()) {
        String custSql = "UPDATE public.customer SET custname = ?, custemail = ?, custaddress = ?, custphoneno = ?, custpassword = ? WHERE custid = ?";

        try (PreparedStatement statement = connection.prepareStatement(custSql)) {
            statement.setString(1, currentCustomer.getCustName());
            statement.setString(2, currentCustomer.getCustEmail());
            statement.setString(3, currentCustomer.getCustAddress());
            statement.setString(4, currentCustomer.getCustPhoneNo());
            statement.setString(5, currentCustomer.getCustPassword());
            statement.setLong(6, currentCustomer.getCustId());
            statement.executeUpdate();
        }
    } catch (SQLException e) {
        logger.error("Failed to update cust", e);
        throw new RuntimeException("Failed to update cust", e);
    }

    session.setAttribute("cust", currentCustomer);
    return "redirect:/custProfile";
}


    @PostMapping("/deleteCustomer")
public String deleteCustomer(HttpSession session) {
    Long custId = (Long) session.getAttribute("custid");
    if (custId == null) {
        logger.warn("custId is null in session");
        return "redirect:/custLogin";
    }

    try (Connection connection = dataSource.getConnection()) {
        String custSql = "DELETE FROM public.customer WHERE custid = ?";
        try (PreparedStatement statement = connection.prepareStatement(custSql)) {
            statement.setLong(1, custId);
            statement.executeUpdate();
        }
    } catch (SQLException e) {
        logger.error("Failed to delete cust", e);
        throw new RuntimeException("Failed to delete cust", e);
    }

    // Invalidate the session
    session.invalidate();

    return "redirect:/custLogin";
}
    
}

