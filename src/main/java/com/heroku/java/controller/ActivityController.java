package com.heroku.java.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.io.IOException;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import com.heroku.java.model.Activity;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Dry;
import com.heroku.java.model.Wet;
import com.heroku.java.model.Staff;

@Controller
public class ActivityController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ActivityController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/listActivity")
    public String listActivities(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null ) {
            return "redirect:/staffLogin";
        }

        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT a.activityid, a.activityname, a.activityduration, a.activityprice, a.activityimage, " +
                     "w.activityequipment, d.activitylocation " +
                     "FROM public.activity a " +
                     "LEFT JOIN public.wet w ON a.activityid = w.activityid " +
                     "LEFT JOIN public.dry d ON a.activityid = d.activityid";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("activityid");
                String name = rs.getString("activityname");
                String duration = rs.getString("activityduration");
                double price = rs.getDouble("activityprice");
                String image = rs.getString("activityimage");
                String equipment = rs.getString("activityequipment");
                String location = rs.getString("activitylocation");

                Activity activity;
                if (equipment != null) {
                    activity = new Wet(id, name, duration, price, image, equipment);
                } else if (location != null) {
                    activity = new Dry(id, name, duration, price, image, location);
                } else {
                    activity = new Activity(id, name, duration, price, image);
                }

                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("activities", activities);
        return "listActivity";
    }

    @GetMapping("/listActivityForCustomer")
    public String listActivitiesForCustomer(HttpSession session, Model model) {
        Customer cust = (Customer) session.getAttribute("cust");
        if (cust == null ) {
            return "redirect:/custLogin";
        }

        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT a.activityid, a.activityname, a.activityduration, a.activityprice, a.activityimage, " +
                     "w.activityequipment, d.activitylocation " +
                     "FROM public.activity a " +
                     "LEFT JOIN public.wet w ON a.activityid = w.activityid " +
                     "LEFT JOIN public.dry d ON a.activityid = d.activityid";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("activityid");
                String name = rs.getString("activityname");
                String duration = rs.getString("activityduration");
                double price = rs.getDouble("activityprice");
                String image = rs.getString("activityimage");
                String equipment = rs.getString("activityequipment");
                String location = rs.getString("activitylocation");

                Activity activity;
                if (equipment != null) {
                    activity = new Wet(id, name, duration, price, image, equipment);
                } else if (location != null) {
                    activity = new Dry(id, name, duration, price, image, location);
                } else {
                    activity = new Activity(id, name, duration, price, image);
                }

                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("activities", activities);
        return "listActivityForCustomer";
    }

    @GetMapping("/createActivity")
    public String createActivityForm(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }
        model.addAttribute("activity", new Activity());
        return "createActivity";
    }

    @PostMapping("/createActivities")
    public String createActivity(HttpSession session, 
                                 @ModelAttribute("activity") 
                                 @RequestParam("name") String activityName,
                                 @RequestParam("duration") String activityDuration,
                                 @RequestParam("price") double activityPrice,
                                 @RequestParam("activityImage") MultipartFile activityImage,
                                 @RequestParam("activityType") String activityType,
                                 @RequestParam(value = "equipment", required = false) String equipment,
                                 @RequestParam(value = "location", required = false) String location) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        // Handle file upload
        /*String activityImagePath = null;
        if (!activityImage.isEmpty()) {
            String uploadDirectory = "src/main/resources/images/"; // Define this directory for saving the image
            try {
                // Ensure the directory exists
                Files.createDirectories(Paths.get(uploadDirectory));
                Path filePath = Paths.get(uploadDirectory + activityImage.getOriginalFilename());
                Files.write(filePath, activityImage.getBytes());
                activityImagePath = activityImage.getOriginalFilename(); // Save just the filename
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception accordingly
            }
        }*/

        // Convert the image to Base64 string
        String imageBase64 = null;
        if (!activityImage.isEmpty()) {
            try {
                byte[] imageBytes = activityImage.getBytes();
                imageBase64 = Base64.getEncoder().encodeToString(imageBytes); // Convert to Base64
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Instantiate the Activity object and set the fields
        Activity activity = new Activity();
        activity.setActivityDuration(activityDuration);
        activity.setActivityName(activityName);
        activity.setActivityPrice(activityPrice);
        activity.setActivityImagePath(imageBase64);

        String sql = "INSERT INTO public.activity(activityduration, activityname, activityprice, activityimage) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement statement = conn.prepareStatement(sql, new String[] {"activityid"})) {
                statement.setString(1, activityDuration);
                statement.setString(2, activityName);
                statement.setDouble(3, activityPrice);
                statement.setString(4, imageBase64);

                // Execute the insert statement
                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long activityId = generatedKeys.getLong(1);

                            if ("wet".equals(activityType)) {
                                String wetSql = "INSERT INTO public.wet(activityid, activityequipment) VALUES (?, ?)";
                                try (PreparedStatement wetStatement = conn.prepareStatement(wetSql)) {
                                    wetStatement.setLong(1, activityId);
                                    wetStatement.setString(2, equipment);
                                    wetStatement.executeUpdate();
                                }

                            } else if ("dry".equals(activityType)) {
                                String drySql = "INSERT INTO public.dry(activityid, activitylocation) VALUES (?, ?)";
                                try (PreparedStatement dryStatement = conn.prepareStatement(drySql)) {
                                    dryStatement.setLong(1, activityId);
                                    dryStatement.setString(2, location);
                                    dryStatement.executeUpdate();
                                }

                            }

                            conn.commit();
                        }
                    }
                } else {
                    conn.rollback();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    conn.rollback();  // Ensure rollback on exception
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "redirect:/listActivity?createSuccess=true";
    }

    @GetMapping("/updateActivity/{id}")
    public String updateActivityForm(@PathVariable Long id, HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        // Fetch the activity by ID
        String sql = "SELECT a.activityid, a.activityname, a.activityduration, a.activityprice, a.activityimage, " +
                    "w.activityequipment, d.activitylocation " +
                    "FROM public.activity a " +
                    "LEFT JOIN public.wet w ON a.activityid = w.activityid " +
                    "LEFT JOIN public.dry d ON a.activityid = d.activityid " +
                    "WHERE a.activityid = ?";
        Activity activity = null;
        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("activityname");
                String duration = rs.getString("activityduration");
                double price = rs.getDouble("activityprice");
                String image = rs.getString("activityimage");
                String equipment = rs.getString("activityequipment");
                String location = rs.getString("activitylocation");

                if (equipment != null) {
                     // set old activityType to be used when submitting updateActivityForm
                    session.setAttribute("oldActivityType", "Wet");
                    activity = new Wet(id, name, duration, price, image, equipment);
                } else if (location != null) {
                    // set old activityType to be used when submitting updateActivityForm
                    session.setAttribute("oldActivityType", "Dry");
                    activity = new Dry(id, name, duration, price, image, location);
                } else {
                    session.setAttribute("oldActivityType", "Activity");
                    activity = new Activity(id, name, duration, price, image);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("activity", activity);
        return "updateActivity";
    }

    @PostMapping("/updateActivity/{id}")
    public String updateActivity(@PathVariable Long id, HttpSession session,
                                @ModelAttribute Activity updatedActivity,
                                @RequestParam("activityType") String activityType,
                                @RequestParam("activityLocation") String activityLocation,
                                @RequestParam("activityEquipment") String activityEquipment,
                                @RequestParam("activityImage") MultipartFile activityImage) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        // SQL for updating activity and the location or equipment activity depending if it's Dry or Wet
        String sql = "UPDATE public.activity SET activityname = ?, activityduration = ?, activityprice = ?, activityimage = ? WHERE activityid = ?;";
        // if ((updatedActivity instanceof Dry || updatedActivity instanceof Activity) && activityType.equals("dry")) {
        if (session.getAttribute("oldActivityType").equals("Dry") && activityType.equals("dry")) {
            System.out.println("Dry and dry");
            sql = "BEGIN TRANSACTION; UPDATE public.activity SET activityname = ?, activityduration = ?, activityprice = ?, activityimage = ? WHERE activityid = ?; UPDATE public.dry SET activityLocation = ? WHERE activityid = ?; COMMIT;";
        // } else if ((updatedActivity instanceof Wet || updatedActivity instanceof Activity) && activityType.equals("wet")) {
        } else if (session.getAttribute("oldActivityType").equals("Wet") && activityType.equals("wet")) {
            System.out.println("Wet and wet");
            sql = "BEGIN TRANSACTION; UPDATE public.activity SET activityname = ?, activityduration = ?, activityprice = ?, activityimage = ? WHERE activityid = ?; UPDATE public.wet SET activityEquipment = ? WHERE activityid = ?; COMMIT;";
        } else if (activityType.equals("dry")) {
            System.out.println("dry");
            sql = "BEGIN TRANSACTION; UPDATE public.activity SET activityname = ?, activityduration = ?, activityprice = ?, activityimage = ? WHERE activityid = ?; DELETE FROM public.wet WHERE EXISTS (SELECT FROM public.wet WHERE activityid = ?); INSERT INTO public.dry(activityid, activitylocation) VALUES (?, ?); COMMIT;";
        } else if (activityType.equals("wet")) {
            System.out.println("wet");
            sql = "BEGIN TRANSACTION; UPDATE public.activity SET activityname = ?, activityduration = ?, activityprice = ?, activityimage = ? WHERE activityid = ?; DELETE FROM public.dry WHERE EXISTS (SELECT FROM public.dry WHERE activityid = ?); INSERT INTO public.wet(activityid, activityequipment) VALUES (?, ?); COMMIT;";
        } else { // activityType is not selected
            System.out.println("activityType is not selected");
            sql = "UPDATE public.activity SET activityname = ?, activityduration = ?, activityprice = ?, activityimage = ? WHERE activityid = ?;";
        }
        
        // Handle activityImagePath if it's empty or not
        String activityImagePath = null;
        if (!activityImage.isEmpty()) {
            // MacOS: This uploadDirectory variable might not work with Windows devices/systems
            String uploadDirectory = "src/main/resources/images/"; // Define this directory for saving the image
            // Windows: Try this if you use Windows devices/systems
            // NOTE: you might need to change the value since it's hard-coded
            // String uploadDirectory = "D:\\kem-tasik-biru\\src\\main\\resources\\uploaded_images\\"; // Define this directory for saving the image
            try {
                Path filePath = Paths.get(uploadDirectory + activityImage.getOriginalFilename());
                Files.write(filePath, activityImage.getBytes());
                activityImagePath = activityImage.getOriginalFilename(); // Save just the filename or relative path
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Retrieve the current image path if no new image is uploaded
            String currentImagePath = updatedActivity.getActivityImagePath();
            activityImagePath = currentImagePath;
        }

        // Set the SQL statement and execute it
        try (Connection conn = dataSource.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, updatedActivity.getActivityName());
                statement.setString(2, updatedActivity.getActivityDuration());
                statement.setDouble(3, updatedActivity.getActivityPrice());
                statement.setString(4, activityImagePath);
                statement.setLong(5, id);

                if (session.getAttribute("oldActivityType").equals("Dry") && activityType.equals("dry")) {
                    statement.setString(6, activityLocation);
                    statement.setLong(7, id);
                } else if (session.getAttribute("oldActivityType").equals("Wet") && activityType.equals("wet")) {
                    statement.setString(6, activityEquipment);
                    statement.setLong(7, id);
                } else if (activityType.equals("dry")) {
                    statement.setLong(6, id); // for DELETE
                    statement.setLong(7, id); // for INSERT
                    statement.setString(8, activityLocation);
                } else if (activityType.equals("wet")) {
                    statement.setLong(6, id); // for DELETE
                    statement.setLong(7, id); // for INSERT
                    statement.setString(8, activityEquipment);
                } else { // every ? field is filled
                    ;
                }

                statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "redirect:/listActivity?updateSuccess=true";
    }

    // @PostMapping("/deleteActivity/{id}")
    @GetMapping("/deleteActivity/{id}")
    public String deleteActivity(@PathVariable("id") Long activityId, HttpSession session) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        // Since we have no way of knowing the type of activity that we want to delete
        // Try to delete from public.dry table first before deleting from public.activity
        try {
            String deleteDrySql = "DELETE FROM public.dry WHERE activityid = ?";
            jdbcTemplate.update(deleteDrySql, activityId);
        } catch (Exception e) {
            logger.info("Exception when trying to delete from public.dry where activityId = {}: {}", activityId, e);
        }

        // Since we have no way of knowing the type of activity that we want to delete
        // Try to delete from public.wet table first before deleting from public.activity
        try {
            String deleteWetSql = "DELETE FROM public.wet WHERE activityid = ?";
            jdbcTemplate.update(deleteWetSql, activityId);
        } catch (Exception e) {
            logger.info("Exception when trying to delete from public.wet where activityId = {}: {}", activityId, e);
        }

        // I don't trust the Java runtime to determine the type of objects :P
        // NOTE: The above code works, but maybe a refactor is needed in the future

        // Delete from public.activity
        String deleteActivitySql = "DELETE FROM public.activity WHERE activityid = ?";
        jdbcTemplate.update(deleteActivitySql, activityId);
        
        return "redirect:/listActivity?deleteSuccess=true";
    }

    
}
