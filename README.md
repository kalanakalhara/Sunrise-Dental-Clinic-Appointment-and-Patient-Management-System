# Sunrise Dental Clinic Appointment & Patient Management System

Complete Java desktop starter implementation based on the final use-case diagram.

## Technology
- Java 21
- JavaFX + FXML
- Maven
- JDBC
- MySQL / MAMP (port 8889)
- JUnit 5

## Features
- Login and credential validation
- ADMIN / RECEPTIONIST / DENTIST roles
- User management
- Patient management
- Dentist and treatment data
- Register/search/delete appointments
- Dentist availability checking
- Billing and treatment charge calculation
- Receipt generation and JavaFX printing
- Help topics
- Support tickets and admin responses/status updates
- Summary reports
- JUnit tests

## Setup
1. Start MAMP MySQL.
2. Import `database/sunrise_dental_clinic.sql` in phpMyAdmin.
3. Open this folder as a Maven project in NetBeans.
4. Run Maven goal `javafx:run`.

Default MAMP connection in `DBConnection.java`:
- Host: localhost
- Port: 8889
- Database: sunrise_dental_clinic_2
- User: root
- Password: root

Demo accounts:
- admin / admin123
- reception / reception123
- dentist1 / dentist123

IMPORTANT: Copy the code into your existing Git repository in stages and make meaningful commits. Do not replace your existing Git history with one final commit.
