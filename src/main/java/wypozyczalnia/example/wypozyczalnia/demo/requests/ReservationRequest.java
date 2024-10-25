package wypozyczalnia.example.wypozyczalnia.demo.requests;

import java.time.LocalDate;

public record ReservationRequest(String userId, String carId, LocalDate reservationDate, LocalDate startingDate,
                                 LocalDate endDate) {
}
