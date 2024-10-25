package wypozyczalnia.example.wypozyczalnia.demo.requests;

public record CarUpdateRequest(String carId, String model, String mark, String numberPlate, Integer price, String description) {
}
