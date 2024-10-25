package wypozyczalnia.example.wypozyczalnia.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Car;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, String> {
    List<Car> findByModel(String model);
    List<Car> findByMark(String mark);
    List<Car> findByModelAndMark(String model, String mark);
    Optional<Car> findByNumberPlate(String numberPlate);
    List<Car> findByPriceLessThan(Integer price);
    List<Car> findByPriceGreaterThan(Integer price);
    List<Car> findByPriceBetween(Integer minPrice, Integer maxPrice);
}
