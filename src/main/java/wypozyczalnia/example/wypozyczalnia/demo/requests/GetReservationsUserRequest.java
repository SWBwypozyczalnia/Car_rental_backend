package wypozyczalnia.example.wypozyczalnia.demo.requests;

import java.time.LocalDate;

public record GetReservationsUserRequest(String userId, LocalDate startDate, LocalDate endDate, String after) {
}
