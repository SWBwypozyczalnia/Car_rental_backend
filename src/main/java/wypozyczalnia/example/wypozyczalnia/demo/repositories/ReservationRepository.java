package wypozyczalnia.example.wypozyczalnia.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Reservation;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Token;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findAllByCarId(String carId);
    List<Reservation> findAllByUserId(String userId);
    List<Reservation> findByCarIdAndStartingDateGreaterThanEqual(
            String carId, LocalDate startDate);
    List<Reservation> findByCarIdAndEndDateGreaterThanEqualAndStartingDateLessThanEqual(
            String userId, LocalDate startDate, LocalDate endDate);
    List<Reservation> findByUserIdAndEndDateGreaterThanEqualAndStartingDateLessThanEqual(
            String userId, LocalDate startDate, LocalDate endDate);

}
