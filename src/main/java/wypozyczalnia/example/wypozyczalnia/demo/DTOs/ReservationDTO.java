package wypozyczalnia.example.wypozyczalnia.demo.DTOs;

import lombok.Data;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Car;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Reservation;
import java.time.LocalDate;

@Data
public class ReservationDTO {
    private String id;
    private UserDTO userDTO;
    private CarDTO carDTO;
    private LocalDate reservationDate;
    private LocalDate startingDate;
    private LocalDate endDate;
    private Boolean picked_up;
    private Boolean returned;

    public ReservationDTO(String id, UserDTO userDTO, Car car, LocalDate reservationDate, LocalDate startingDate,
                          LocalDate endDate, Boolean picked_up, Boolean returned) {
        this.id = id;
        this.carDTO = new CarDTO(car);
        this.reservationDate = reservationDate;
        this.startingDate = startingDate;
        this.endDate = endDate;
        this.picked_up = picked_up;
        this.returned = returned;
        this.userDTO = userDTO;
    }

    public ReservationDTO(Reservation reservation, UserDTO userDTO, CarDTO carDTO) {
        this.id = reservation.getId();
        this.carDTO= carDTO;
        this.reservationDate = reservation.getReservatinDate();
        this.startingDate = reservation.getStartingDate();
        this.endDate = reservation.getEndDate();
        this.picked_up = reservation.isPickedUp();
        this.returned = reservation.isReturned();
        this.userDTO = userDTO;
    }
}
