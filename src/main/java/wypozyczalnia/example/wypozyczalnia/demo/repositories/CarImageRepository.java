package wypozyczalnia.example.wypozyczalnia.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wypozyczalnia.example.wypozyczalnia.demo.entities.CarImages;

public interface CarImageRepository extends JpaRepository<CarImages, String> {
}
