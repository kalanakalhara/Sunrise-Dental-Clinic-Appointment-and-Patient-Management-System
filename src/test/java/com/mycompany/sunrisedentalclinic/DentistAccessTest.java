package com.mycompany.sunrisedentalclinic;

import com.mycompany.sunrisedentalclinic.model.Dentist;
import com.mycompany.sunrisedentalclinic.model.User;
import com.mycompany.sunrisedentalclinic.service.AppointmentService;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DentistAccessTest {
    private final AppointmentService service = new AppointmentService();

    private Dentist dentist(int id, String name, String email) {
        return new Dentist(id, name, "General", "", email, LocalTime.of(9, 0), LocalTime.of(17, 0));
    }

    private User user(String name, String email) {
        return new User(99, "dentist", name, email, "DENTIST", true);
    }

    @Test
    void matchesDentistProfileByEmailInsteadOfUserIdOrSharedName() {
        assertEquals(7, service.dentistIdFor(user("Same name", "one@example.com"), List.of(
                dentist(2, "Same name", "two@example.com"),
                dentist(7, "Same name", "ONE@example.com"))));
    }

    @Test
    void missingEmailCanUseUniqueLegacyName() {
        assertEquals(7, service.dentistIdFor(user("Dr. One", null),
                List.of(dentist(7, "Dr. One", ""))));
    }

    @Test
    void mismatchedEmailDoesNotFallBackToAnotherDentistsName() {
        assertThrows(SecurityException.class, () -> service.dentistIdFor(user("Dr. One", "wrong@example.com"),
                List.of(dentist(7, "Dr. One", "one@example.com"))));
    }

    @Test
    void missingAndAmbiguousProfilesAreRejected() {
        assertThrows(SecurityException.class, () -> service.dentistIdFor(user("Dr. One", ""), List.of()));
        assertThrows(SecurityException.class, () -> service.dentistIdFor(user("Dr. One", ""), List.of(
                dentist(1, "Dr. One", ""), dentist(2, "Dr. One", ""))));
        assertThrows(SecurityException.class, () -> service.dentistIdFor(user("Dr. One", "one@example.com"), List.of(
                dentist(1, "Dr. One", "one@example.com"), dentist(2, "Dr. Two", "one@example.com"))));
    }

    @Test
    void unauthenticatedOrInactiveUsersCannotResolveDentistAccess() {
        assertThrows(SecurityException.class, () -> service.dentistIdFor(null, List.of()));
        assertThrows(SecurityException.class, () -> service.dentistIdFor(
                new User(1, "x", "Dr. One", "", "DENTIST", false), List.of(dentist(1, "Dr. One", ""))));
        assertThrows(SecurityException.class, () -> service.dentistIdFor(
                new User(1, "x", "Dr. One", "", "RECEPTIONIST", true), List.of(dentist(1, "Dr. One", ""))));
    }
}
