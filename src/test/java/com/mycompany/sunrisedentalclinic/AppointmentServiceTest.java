package com.mycompany.sunrisedentalclinic;

import com.mycompany.sunrisedentalclinic.dao.ClinicDAO;
import com.mycompany.sunrisedentalclinic.service.AppointmentService;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {

    @Test
    void unavailableDentistCannotBeBookedEvenWithAnOpenSlot() {
        var dentist = new com.mycompany.sunrisedentalclinic.model.Dentist(1, "Dentist", "General", "", "",
                LocalTime.of(9, 0), LocalTime.of(17, 0), false);
        var service = new AppointmentService();
        var error = assertThrows(IllegalArgumentException.class, () -> service.validateSlot(
                dentist, LocalDate.now().plusDays(1), LocalTime.of(10, 0), true));
        assertEquals("The selected dentist is unavailable.", error.getMessage());
        assertEquals("Unavailable", dentist.availability());
    }

    @Test
    void availableDentistCanBeBookedWithinWorkingHours() {
        var dentist = new com.mycompany.sunrisedentalclinic.model.Dentist(1, "Dentist", "General", "", "",
                LocalTime.of(9, 0), LocalTime.of(17, 0), true);
        assertDoesNotThrow(() -> new AppointmentService().validateSlot(
                dentist, LocalDate.now().plusDays(1), LocalTime.of(10, 0), true));
    }

    @Test
    void availableAppointmentShouldBeAccepted()
            throws SQLException {

        ClinicDAO fakeDAO = new ClinicDAO() {

            @Override
            public boolean withinDentistHours(
                    int dentistId,
                    LocalTime time
            ) {
                return true;
            }

            @Override
            public boolean slotAvailable(
                    int dentistId,
                    LocalDate date,
                    LocalTime time
            ) {
                return true;
            }
        };

        AppointmentService service =
                new AppointmentService(fakeDAO);

        assertDoesNotThrow(() ->
                service.validateAppointment(
                        1,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );
    }

    @Test
    void doubleBookedSlotShouldBeRejected()
            throws SQLException {

        ClinicDAO fakeDAO = new ClinicDAO() {

            @Override
            public boolean withinDentistHours(
                    int dentistId,
                    LocalTime time
            ) {
                return true;
            }

            @Override
            public boolean slotAvailable(
                    int dentistId,
                    LocalDate date,
                    LocalTime time
            ) {
                return false;
            }
        };

        AppointmentService service =
                new AppointmentService(fakeDAO);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.validateAppointment(
                                1,
                                LocalDate.now().plusDays(1),
                                LocalTime.of(10, 0)
                        )
                );

        assertEquals(
                "Dentist is already booked for this time",
                exception.getMessage()
        );
    }

    @Test
    void appointmentOutsideDentistHoursShouldBeRejected()
            throws SQLException {

        ClinicDAO fakeDAO = new ClinicDAO() {

            @Override
            public boolean withinDentistHours(
                    int dentistId,
                    LocalTime time
            ) {
                return false;
            }

            @Override
            public boolean slotAvailable(
                    int dentistId,
                    LocalDate date,
                    LocalTime time
            ) {
                return true;
            }
        };

        AppointmentService service =
                new AppointmentService(fakeDAO);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.validateAppointment(
                                1,
                                LocalDate.now().plusDays(1),
                                LocalTime.of(20, 0)
                        )
                );

        assertEquals(
                "Appointment is outside dentist working hours",
                exception.getMessage()
        );
    }

    @Test
    void pastAppointmentDateShouldBeRejected()
            throws SQLException {

        AppointmentService service =
                new AppointmentService(new ClinicDAO());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.validateAppointment(
                                1,
                                LocalDate.now().minusDays(1),
                                LocalTime.of(10, 0)
                        )
                );

        assertEquals(
                "Appointment date cannot be in the past",
                exception.getMessage()
        );
    }

    @Test
    void missingAppointmentDateShouldBeRejected() {

        AppointmentService service =
                new AppointmentService(new ClinicDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateAppointment(
                        1,
                        null,
                        LocalTime.of(10, 0)
                )
        );
    }

    @Test
    void missingAppointmentTimeShouldBeRejected() {

        AppointmentService service =
                new AppointmentService(new ClinicDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateAppointment(
                        1,
                        LocalDate.now().plusDays(1),
                        null
                )
        );
    }

    @Test
    void invalidDentistShouldBeRejected() {

        AppointmentService service =
                new AppointmentService(new ClinicDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateAppointment(
                        0,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );
    }
}