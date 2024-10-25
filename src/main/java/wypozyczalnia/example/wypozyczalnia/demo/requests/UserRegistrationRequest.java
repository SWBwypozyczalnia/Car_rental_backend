package wypozyczalnia.example.wypozyczalnia.demo.requests;

import java.time.LocalDate;

public record UserRegistrationRequest(
        String firstName, String lastName, String email, String phoneNumber, String password, String repPassword
        ) {
}