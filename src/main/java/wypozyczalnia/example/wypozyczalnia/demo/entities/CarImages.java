package wypozyczalnia.example.wypozyczalnia.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carImages")
public class CarImages {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "imagePath", nullable = false)
    private String imagePath;

    @Column(name = "mainPicture", nullable = false)
    private Boolean mainPicture = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name ="car_id", nullable = false)
    private Car car;
}
