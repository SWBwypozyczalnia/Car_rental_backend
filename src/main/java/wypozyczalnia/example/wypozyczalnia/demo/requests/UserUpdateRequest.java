package wypozyczalnia.example.wypozyczalnia.demo.requests;

import java.time.LocalDate;

public record UserUpdateRequest(
        String userId, String firstName, String lastName, String email, String password, String repPassword,
        String phoneNumber) {
}
