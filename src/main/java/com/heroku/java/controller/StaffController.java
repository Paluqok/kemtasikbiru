package com.heroku.java.controller;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.heroku.java.model.Staff;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;

@Controller
public class StaffController {

    private static final Logger logger = LoggerFactory.getLogger(StaffController.class);
    private final DataSource dataSource;

    @Autowired
    public StaffController(DataSource dataSource) {
        this.dataSource = dataSource;
    }
     
    @GetMapping("/staffSignUp")
    public String createStaffAccount(Model model){
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT staffid, staffname FROM public.staff WHERE managerid IS NULL";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                List<Staff> managers = new ArrayList<>();
                while (resultSet.next()) {
                    Staff manager = new Staff();
                    manager.setStaffId(resultSet.getLong("staffid"));
                    manager.setStaffName(resultSet.getString("staffname"));
                    managers.add(manager);
                }
                model.addAttribute("managers", managers);
            }
        } catch (SQLException e) {
            logger.error("Error fetching managers", e);
        }
        return "staff/staffSignUp";
    }

    @PostMapping("/createAccountStaff")
    public String createAccountStaff(@ModelAttribute("createAccountStaff") @RequestParam("staffName") String staffName,
                                   @RequestParam("staffEmail") String staffEmail,
                                   @RequestParam("staffAddress") String staffAddress,
                                   @RequestParam("staffPhoneNo") String staffPhoneNo,
                                   @RequestParam("staffPassword") String staffPassword,
                                   @RequestParam("userType") String userType,
                                   @RequestParam(value = "managerId", required = false) Long managerId) throws IOException {
        
        Staff staff = new Staff();
        staff.setStaffName(staffName);
        staff.setStaffEmail(staffEmail);
        staff.setStaffAddress(staffAddress);
        staff.setStaffPhoneNo(staffPhoneNo);
        staff.setStaffPassword(staffPassword);

        Long assignedManagerId = "staff".equals(userType) ? managerId : null;

        try (Connection connection = dataSource.getConnection()) {
            String staffSql = "INSERT INTO public.staff(staffname, staffemail, staffaddress, staffphoneno, staffpassword, managerid) VALUES (?, ?, ?, ?, ?, ?) RETURNING staffid";

            System.out.println("Received staff details:");
            System.out.println("Name: " + staff.getStaffName());
            System.out.println("Email: " + staff.getStaffEmail());
            System.out.println("Address: " + staff.getStaffAddress());
            System.out.println("Phone No: " + staff.getStaffPhoneNo());
            System.out.println("Password: " + staff.getStaffPassword());


            try (PreparedStatement statement = connection.prepareStatement(staffSql)) {
                statement.setString(1, staff.getStaffName());
                statement.setString(2, staff.getStaffEmail());
                statement.setString(3, staff.getStaffAddress());
                statement.setString(4, staff.getStaffPhoneNo());
                statement.setString(5, staff.getStaffPassword());
                statement.setObject(6, assignedManagerId); // Set the Base64-encoded image

                if ("staff".equals(userType)) {
                    statement.setLong(6, managerId);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Long staffId = resultSet.getLong("staffid");
                        staff.setStaffId(staffId);
                        System.out.println("Inserted staff with ID: " + staffId);
                    } else {
                        throw new SQLException("Failed to insert staff, no ID obtained.");
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
            // Handle exception appropriately, e.g., log error or rethrow as runtime exception
            throw new RuntimeException("Failed to insert staff", e);
        }

        return "redirect:/staffLogin"; // Return the created staff object
    }

    @GetMapping("/staffLogin")
    public String staffLogin() {
        return "staff/staffLogin";
    }

    @PostMapping("/loginStaff")
    public String staffLogins(HttpSession session,
                              @RequestParam("staffEmail") String staffEmail,
                              @RequestParam("staffPassword") String staffPassword) throws LoginException {

        logger.info("Attempting to log in staff with email: {}", staffEmail);

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT staffid, staffname, staffemail, staffphoneno, staffaddress, staffpassword FROM public.staff WHERE staffemail = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, staffEmail);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Staff staff = new Staff();
                        staff.setStaffId(resultSet.getLong("staffid"));
                        staff.setStaffName(resultSet.getString("staffname"));
                        staff.setStaffEmail(resultSet.getString("staffemail"));
                        staff.setStaffPhoneNo(resultSet.getString("staffphoneno"));
                        staff.setStaffAddress(resultSet.getString("staffaddress"));
                        staff.setStaffPassword(resultSet.getString("staffpassword"));

                        logger.info("Staff found: {}", staff.getStaffName());

                        if (staff.getStaffPassword().equals(staffPassword)) {
                            logger.info("Password matches for staff: {}", staff.getStaffName());
                            session.setAttribute("staffname", staff.getStaffName());
                            session.setAttribute("staffid", staff.getStaffId());
                            session.setAttribute("staff", staff);
                            return "redirect:/homeStaff";
                        } else {
                            logger.warn("Password does not match for staff: {}", staff.getStaffName());
                        }
                    } else {
                        logger.warn("No staff found with email: {}", staffEmail);
                    }
                }
            }
            return "redirect:/staffLogin";
        } catch (SQLException e) {
            logger.error("Error during staff login", e);
            return "redirect:/error";
        }
    }

    @GetMapping("/homeStaff")
    public String homeStaff(HttpSession session, Model model) {
        Long staffId = (Long) session.getAttribute("staffid");
        if (staffId == null) {
            logger.warn("staffId is null in session");
            return "redirect:/staffLogin";
        }

        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            logger.warn("staff is null in session");
            return "redirect:/staffLogin";
        }

        model.addAttribute("staff", staff);
        return "homeStaff";
    }

    @GetMapping("/staffProfile")
    public String staffProfile(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }
        model.addAttribute("staff", staff);
        return "staff/staffProfile";
    }

    // Serve the update form with current data
@GetMapping("/staffUpdate")
public String showUpdateForm(HttpSession session, Model model) {
    Staff staff = (Staff) session.getAttribute("staff");
    if (staff == null) {
        return "redirect:/staffLogin";
    }
    model.addAttribute("staff", staff);
    return "staff/staffUpdate";
}

// Process the update form
@PostMapping("/updateStaff")
public String updateStaff(
    @RequestParam("staffId") Long staffId,
    @RequestParam("staffName") String staffName,
    @RequestParam("staffEmail") String staffEmail,
    @RequestParam("staffAddress") String staffAddress,
    @RequestParam("staffPhoneNo") String staffPhoneNo,
    @RequestParam(value = "staffPassword", required = false) String staffPassword,
    @RequestParam(value = "checkPassword", required = false) String checkPassword,
    HttpSession session) throws IOException {

    // Fetch the current staff object from the session
    Staff currentStaff = (Staff) session.getAttribute("staff");
    if (currentStaff == null) {
        return "redirect:/staffLogin";
    }

    // Check that the staffId matches the ID of the staff in the session
    if (!staffId.equals(currentStaff.getStaffId())) {
        return "redirect:/staffProfile";
    }

    // Update the staff details
    currentStaff.setStaffName(staffName);
    currentStaff.setStaffEmail(staffEmail);
    currentStaff.setStaffAddress(staffAddress);
    currentStaff.setStaffPhoneNo(staffPhoneNo);

    // Update password only if provided and passwords match
    if (staffPassword != null && !staffPassword.isEmpty()) {
        if (staffPassword.equals(checkPassword)) {
            currentStaff.setStaffPassword(staffPassword);
        } else {
            // Add an error message to the model if passwords do not match
            return "redirect:/staffUpdate?error=true";
        }
    }

    // Save the updated staff object
    try (Connection connection = dataSource.getConnection()) {
        String staffSql = "UPDATE public.staff SET staffname = ?, staffemail = ?, staffaddress = ?, staffphoneno = ?, staffpassword = ? WHERE staffid = ?";

        try (PreparedStatement statement = connection.prepareStatement(staffSql)) {
            statement.setString(1, currentStaff.getStaffName());
            statement.setString(2, currentStaff.getStaffEmail());
            statement.setString(3, currentStaff.getStaffAddress());
            statement.setString(4, currentStaff.getStaffPhoneNo());
            statement.setString(5, currentStaff.getStaffPassword());
            statement.setLong(6, currentStaff.getStaffId());
            statement.executeUpdate();
        }
    } catch (SQLException e) {
        logger.error("Failed to update staff", e);
        throw new RuntimeException("Failed to update staff", e);
    }

    session.setAttribute("staff", currentStaff);
    return "redirect:/staffProfile";
}


    @PostMapping("/deleteStaff")
public String deleteStaff(HttpSession session) {
    Long staffId = (Long) session.getAttribute("staffid");
    if (staffId == null) {
        logger.warn("staffId is null in session");
        return "redirect:/staffLogin";
    }

    try (Connection connection = dataSource.getConnection()) {
        String staffSql = "DELETE FROM public.staff WHERE staffid = ?";
        try (PreparedStatement statement = connection.prepareStatement(staffSql)) {
            statement.setLong(1, staffId);
            statement.executeUpdate();
        }
    } catch (SQLException e) {
        logger.error("Failed to delete staff", e);
        throw new RuntimeException("Failed to delete staff", e);
    }

    // Invalidate the session
    session.invalidate();

    return "redirect:/staffLogin";
}
    
}

