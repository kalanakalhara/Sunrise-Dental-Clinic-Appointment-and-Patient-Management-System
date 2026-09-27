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

## Dentist appointments
- Dentists see only appointments assigned to their dentist profile, including dashboard and search results.
- Select an appointment and click **Edit appointment** to update status/notes and add treatments. Use Cmd/Ctrl-click to select multiple treatments. Existing treatments are retained; selecting an existing treatment does not charge it twice.
- Billing includes the original and additional treatments. Additional treatments cannot be added after payment.
- Login profiles match dentist records by email (or a unique name when the login has no email). Keep these details consistent; missing or ambiguous matches deny access.
- Existing databases automatically create the `appointment_treatments` table on first use. Fresh installations include it in the SQL setup script.

## Role permissions
- Admin: all sections; add, edit, and delete treatments.
- Receptionist: dentist directory and working hours; treatment list is read-only.
- Dentist: own appointments; add and edit treatments; no treatment deletion or Dentists section.
- Restricted sections and action controls are hidden. Action handlers also check permissions.

## Deleting users
Deleting a user removes their support tickets. For a dentist account, deletion also removes the matching dentist profile, its appointments, appointment treatment entries, and bills. Patient records and the shared treatment catalog remain. All removals run in one transaction; a failure rolls everything back. Legacy dentist profiles are matched by email, or by name when the account has no email; ambiguous or shared matches block deletion.
