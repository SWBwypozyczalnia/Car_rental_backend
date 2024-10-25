package wypozyczalnia.example.wypozyczalnia.demo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Car;
import wypozyczalnia.example.wypozyczalnia.demo.entities.CarImages;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDTO {
    private String id;
    private String model;
    private String mark;
    private String numberPlate;
    private Integer price;
    private String description;
    private List<String> imageUuids;
    private String mainImageUuid;

    public CarDTO(Car car) {
        this.id = car.getId();
        this.model = car.getModel();
        this.mark = car.getMark();
        this.numberPlate = car.getNumberPlate();
        this.price = car.getPrice();
        this.description = car.getDescription();
        this.imageUuids = car.getImages().stream()
                .map(CarImages::getId)
                .collect(Collectors.toList());
        this.mainImageUuid = car.getImages().stream()
                .filter(CarImages::getMainPicture) // <-- tutaj bezpośrednie użycie pola
                .map(CarImages::getId)
                .findFirst()
                .orElse(null);
    }
}
