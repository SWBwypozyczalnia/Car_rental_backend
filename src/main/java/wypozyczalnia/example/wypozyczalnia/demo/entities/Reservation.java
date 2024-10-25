package wypozyczalnia.example.wypozyczalnia.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservatinDate;

    @Column(name = "starting_date", nullable = false)
    private LocalDate startingDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name="picked_up", nullable = false)
    private boolean pickedUp;

    @Column(name="returned", nullable = false)
    private boolean returned;

}
