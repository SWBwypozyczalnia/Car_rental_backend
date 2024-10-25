package wypozyczalnia.example.wypozyczalnia.demo.requests;

import java.time.LocalDate;

public record ReservationUpdateRequest(String reservationId, LocalDate startingDate, LocalDate endDate) {
}
