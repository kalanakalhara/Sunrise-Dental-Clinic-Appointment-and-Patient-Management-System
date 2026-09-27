package com.mycompany.sunrisedentalclinic.dao;

import com.mycompany.sunrisedentalclinic.model.User;
import com.mycompany.sunrisedentalclinic.util.DBConnection;
import com.mycompany.sunrisedentalclinic.util.PasswordUtil;
import java.sql.*;
import java.util.*;

public class UserDAO {

    public User authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT user_id,username,full_name,email,role,status FROM users WHERE username=? AND password=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, passwordHash);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT user_id,username,full_name,email,role,status FROM users ORDER BY user_id"); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                out.add(map(r));
            }
        }
        return out;
    }

    public void create(String username, String rawPassword, String fullName, String email, String role) throws SQLException {
        String sql = "INSERT INTO users(username,password,full_name,email,role,status) VALUES(?,?,?,?,?,1)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, PasswordUtil.hashPassword(rawPassword));
            p.setString(3, fullName);
            p.setString(4, email);
            p.setString(5, role);
            p.executeUpdate();
        }
    }

    public void update(int id, String username, String rawPassword, String fullName, String email, String role, boolean active) throws SQLException {
        boolean changePassword = rawPassword != null && !rawPassword.isBlank();
        String sql = changePassword
                ? "UPDATE users SET username=?,password=?,full_name=?,email=?,role=?,status=? WHERE user_id=?"
                : "UPDATE users SET username=?,full_name=?,email=?,role=?,status=? WHERE user_id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            int i = 1;
            p.setString(i++, username);
            if (changePassword) {
                p.setString(i++, PasswordUtil.hashPassword(rawPassword));
            }
            p.setString(i++, fullName);
            p.setString(i++, email);
            p.setString(i++, role);
            p.setBoolean(i++, active);
            p.setInt(i, id);
            p.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                User selected;
                // Lock the account so new tickets cannot be linked during deletion.
                try (PreparedStatement user = c.prepareStatement(
                        "SELECT user_id,username,full_name,email,role,status FROM users WHERE user_id=? FOR UPDATE")) {
                    user.setInt(1, id);
                    try (ResultSet result = user.executeQuery()) {
                        if (!result.next()) throw new SQLException("User was not found.");
                        selected = map(result);
                    }
                }
                if ("DENTIST".equals(selected.role())) {
                    deleteDentistRecords(c, selected);
                }
                try (PreparedStatement tickets = c.prepareStatement(
                        "DELETE FROM support_tickets WHERE created_by=?");
                        PreparedStatement user = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {
                    tickets.setInt(1, id);
                    tickets.executeUpdate();
                    user.setInt(1, id);
                    if (user.executeUpdate() != 1) throw new SQLException("User was not found.");
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void deleteDentistRecords(Connection c, User user) throws SQLException {
        // Legacy dentist profiles have no user_id: only delete an unambiguous match.
        boolean hasEmail = user.email() != null && !user.email().isBlank();
        String match = hasEmail ? "LOWER(email)=LOWER(?)" : "LOWER(full_name)=LOWER(?)";
        String value = hasEmail ? user.email() : user.fullName();
        Integer dentistId = null;
        try (PreparedStatement p = c.prepareStatement(
                "SELECT dentist_id FROM dentists WHERE " + match + " FOR UPDATE")) {
            p.setString(1, value);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) dentistId = r.getInt(1);
                if (r.next()) throw new SQLException("Multiple dentist profiles match this account. Correct the profiles before deleting.");
            }
        }
        if (dentistId == null) return;
        try (PreparedStatement p = c.prepareStatement(
                "SELECT user_id FROM users WHERE user_id<>? AND role='DENTIST' AND " + match + " FOR UPDATE")) {
            p.setInt(1, user.userId());
            p.setString(2, value);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) throw new SQLException("This dentist profile is shared by another account. Correct the accounts before deleting.");
            }
        }
        // Lock appointments before removing their dependent rows.
        try (PreparedStatement p = c.prepareStatement(
                "SELECT appointment_id FROM appointments WHERE dentist_id=? FOR UPDATE")) {
            p.setInt(1, dentistId);
            try (ResultSet r = p.executeQuery()) { while (r.next()) { /* acquire all row locks */ } }
        }
        boolean hasAdditionalTreatments;
        try (ResultSet tables = c.getMetaData().getTables(c.getCatalog(), null, "appointment_treatments", new String[]{"TABLE"})) {
            hasAdditionalTreatments = tables.next();
        }
        if (hasAdditionalTreatments) {
            deleteByDentist(c, "DELETE FROM appointment_treatments WHERE appointment_id IN "
                    + "(SELECT appointment_id FROM appointments WHERE dentist_id=?)", dentistId);
        }
        deleteByDentist(c, "DELETE FROM bills WHERE appointment_id IN "
                + "(SELECT appointment_id FROM appointments WHERE dentist_id=?)", dentistId);
        deleteByDentist(c, "DELETE FROM appointments WHERE dentist_id=?", dentistId);
        deleteByDentist(c, "DELETE FROM dentists WHERE dentist_id=?", dentistId);
    }

    private void deleteByDentist(Connection c, String sql, int dentistId) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, dentistId);
            p.executeUpdate();
        }
    }

    private User map(ResultSet r) throws SQLException {
        return new User(r.getInt("user_id"), r.getString("username"), r.getString("full_name"),
                r.getString("email"), r.getString("role"), r.getBoolean("status"));
    }
}
