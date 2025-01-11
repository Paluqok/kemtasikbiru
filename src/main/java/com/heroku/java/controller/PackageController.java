package com.heroku.java.controller;

import com.heroku.java.model.Package;
import com.heroku.java.model.Activity;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PackageController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Package> packageRowMapper = new RowMapper<>() {
        @Override
        public Package mapRow(ResultSet rs, int rowNum) throws SQLException {
            Package pkg = new Package();
            pkg.setPackageId(rs.getLong("packageid"));
            pkg.setPackageName(rs.getString("packagename"));
            pkg.setPackagePrice(rs.getDouble("packageprice"));
            // Activities will be set separately
            return pkg;
        }
    };

    private final RowMapper<Activity> activityRowMapper = new RowMapper<>() {
        @Override
        public Activity mapRow(ResultSet rs, int rowNum) throws SQLException {
            Activity activity = new Activity();
            activity.setActivityId(rs.getLong("activityid"));
            activity.setActivityName(rs.getString("activityname"));
            activity.setActivityDuration(rs.getString("activityduration"));
            activity.setActivityPrice(rs.getDouble("activityprice"));
            activity.setActivityImagePath(rs.getString("activityimage"));
            return activity;
        }
    };

    @GetMapping("/listPackages")
    public String listPackages(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM package";
        List<Package> packages = jdbcTemplate.query(sql, packageRowMapper);

        for (Package pkg : packages) {
            String actIdSql = "SELECT activityid FROM packageactivity WHERE packageid = " + pkg.getPackageId();
            List<Integer> actId = jdbcTemplate.queryForList(actIdSql, Integer.class);
            List<Activity> acts = new ArrayList<Activity>();
            for (Integer i : actId) {
                String actSql = "SELECT * FROM activity WHERE activityid = " + i;
                Activity act = jdbcTemplate.queryForObject(actSql, activityRowMapper);
                acts.add(act);
            }
            pkg.setActivities(acts);
        }
        model.addAttribute("packages", packages);

        return "listPackage";
    }

    @GetMapping("/listPackagesForCustomer")
    public String listPackagesForCustomer(HttpSession session, Model model) {
        Customer cust = (Customer) session.getAttribute("cust");
        if (cust == null ) {
            return "redirect:/custLogin";
        }

        String sql = "SELECT * FROM package";
        List<Package> packages = jdbcTemplate.query(sql, packageRowMapper);

        for (Package pkg : packages) {
            String actIdSql = "SELECT activityid FROM packageactivity WHERE packageid = " + pkg.getPackageId();
            List<Integer> actId = jdbcTemplate.queryForList(actIdSql, Integer.class);
            List<Activity> acts = new ArrayList<Activity>();
            for (Integer i : actId) {
                String actSql = "SELECT * FROM activity WHERE activityid = " + i;
                Activity act = jdbcTemplate.queryForObject(actSql, activityRowMapper);
                acts.add(act);
            }
            pkg.setActivities(acts);
        }
        model.addAttribute("packages", packages);
        
        return "listPackageForCustomer";
    }

    @GetMapping("/createPackage")
    public String createPackageForm(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM activity";
        List<Activity> activities = jdbcTemplate.query(sql, activityRowMapper);
        model.addAttribute("activities", activities);
        return "createPackage";
    }

    @PostMapping("/createPackage")
    public String createPackage(HttpSession session, @RequestParam String packageName, @RequestParam List<Long> activityIds) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM activity WHERE activityid IN (" + 
                  activityIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        List<Activity> selectedActivities = jdbcTemplate.query(sql, activityRowMapper);
        double packagePrice = selectedActivities.stream()
            .mapToDouble(Activity::getActivityPrice)
            .sum();

        String insertPackageSql = "INSERT INTO package (packagename, packageprice) VALUES (?, ?)";
        jdbcTemplate.update(insertPackageSql, packageName, packagePrice);

        Long packageId = jdbcTemplate.queryForObject("SELECT currval(pg_get_serial_sequence('package','packageid'))", Long.class);
        for (Activity activity : selectedActivities) {
            String insertPackageActivitySql = "INSERT INTO packageactivity (packageid, activityid) VALUES (?, ?)";
            jdbcTemplate.update(insertPackageActivitySql, packageId, activity.getActivityId());
        }

        return "redirect:/listPackages?createSuccess=true";
    }

    @GetMapping("/updatePackage/{packageId}")
    public String updatePackageForm(@PathVariable Long packageId, HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM package WHERE packageid = ?";
        Package pkg = jdbcTemplate.queryForObject(sql, packageRowMapper, packageId);

        String actIdSql = "SELECT activityid FROM packageactivity WHERE packageid = " + pkg.getPackageId();
        List<Long> actIds = jdbcTemplate.queryForList(actIdSql, Long.class);
        List<Activity> acts = new ArrayList<Activity>();
        for (Long i : actIds) {
            String actSql = "SELECT * FROM activity WHERE activityid = " + i;
            Activity act = jdbcTemplate.queryForObject(actSql, activityRowMapper);
            acts.add(act);
        }
        pkg.setActivities(acts);

        String activitySql = "SELECT * FROM activity";
        List<Activity> activities = jdbcTemplate.query(activitySql, activityRowMapper);
        
        List<Activity> chosenActivities = pkg.getActivities();
        List<Activity> unchosenActivities = new ArrayList<Activity>();
        unchosenActivities.addAll(activities);
        // for (int i = 0; i < activities.size(); i++) {
        for (Activity a : activities) {
            if (actIds.contains(a.getActivityId())) {
                unchosenActivities.remove(a);
            }
        }

        model.addAttribute("package", pkg);
        model.addAttribute("chosenActivities", chosenActivities);
        model.addAttribute("activities", unchosenActivities);
        return "updatePackage";
    }

    @PostMapping("/updatePackage/{packageId}")
    public String updatePackage(HttpSession session, @RequestParam Long packageId, @RequestParam String packageName, @RequestParam List<Long> activityIds) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        String sql = "SELECT * FROM activity WHERE activityid IN (" + 
                      activityIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        List<Activity> selectedActivities = jdbcTemplate.query(sql, activityRowMapper);
        double packagePrice = selectedActivities.stream()
                .mapToDouble(Activity::getActivityPrice)
                .sum();

        String updatePackageSql = "UPDATE package SET packagename = ?, packageprice = ? WHERE packageid = ?";
        jdbcTemplate.update(updatePackageSql, packageName, packagePrice, packageId);

        String deletePackageActivitySql = "DELETE FROM packageactivity WHERE packageid = ?";
        jdbcTemplate.update(deletePackageActivitySql, packageId);

        for (Activity activity : selectedActivities) {
            String insertPackageActivitySql = "INSERT INTO packageactivity (packageid, activityid) VALUES (?, ?)";
            jdbcTemplate.update(insertPackageActivitySql, packageId, activity.getActivityId());
        }

        return "redirect:/listPackages?updateSuccess=true";
    }

    @GetMapping("/deletePackage/{packageId}")
    public String deletePackage(@PathVariable Long packageId, HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("staff");
        if (staff == null) {
            return "redirect:/staffLogin";
        }

        // Check if package has been booked
        String checkBookingSql = "SELECT COUNT(*) FROM booking WHERE packageid = ?";
        Integer bookingCount = jdbcTemplate.queryForObject(checkBookingSql, Integer.class, packageId);

        if (bookingCount > 0) {
        // Package has been booked, redirect with an error message
            model.addAttribute("deleteFailed", true);
            return "redirect:/listPackages";
        }

        String deletePackageActivitySql = "DELETE FROM packageactivity WHERE packageid = ?";
        jdbcTemplate.update(deletePackageActivitySql, packageId);

        String deletePackageSql = "DELETE FROM package WHERE packageid = ?";
        jdbcTemplate.update(deletePackageSql, packageId);

        return "redirect:/listPackages?deleteSuccess=true";
    }
}
