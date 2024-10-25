package wypozyczalnia.example.wypozyczalnia.demo.DTOs;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReservationListDTO {
    List<ReservationDTO> reservations;
    Boolean lastPage;
    public ReservationListDTO() {
        this.reservations = new ArrayList<>();
        this.lastPage = false;
    }
    public ReservationListDTO(List<ReservationDTO> reservations, Boolean lastPage) {
        this.reservations = reservations;
        this.lastPage = lastPage;
    }
}
