package wypozyczalnia.example.wypozyczalnia.demo.requests;

import java.time.LocalDate;

public record GetReservationsCarRequest(String carId, LocalDate startDate) {
}
