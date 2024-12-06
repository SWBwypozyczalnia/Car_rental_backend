package wypozyczalnia.example.wypozyczalnia.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "model", nullable = false)
    private String model;

    @Column (name ="mark", nullable = false)
    private String mark;

    @Column (name ="number_plate", nullable = false, unique = true)
    private String numberPlate;

    @Column (name ="price", nullable = false)
    private Integer price;

    @Column (name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<CarImages> images;
}
