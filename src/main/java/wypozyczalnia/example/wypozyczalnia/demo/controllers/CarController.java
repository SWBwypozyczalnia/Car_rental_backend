package wypozyczalnia.example.wypozyczalnia.demo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Car;
import wypozyczalnia.example.wypozyczalnia.demo.repositories.CarRepository;
import wypozyczalnia.example.wypozyczalnia.demo.requests.*;
import wypozyczalnia.example.wypozyczalnia.demo.services.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/cars")
public class CarController {
    private final CarService carService;
    private final TokenService tokenService;
    private final CarRepository carRepository;

    public CarController(CarService carService, TokenService tokenService, CarRepository carRepository){
        this.carService = carService;
        this.tokenService = tokenService;
        this.carRepository = carRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCar(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @RequestParam("model") String model,
            @RequestParam("mark") String mark,
            @RequestParam("numberPlate") String numberPlate,
            @RequestParam("price") Integer price,
            @RequestParam("description") String description,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        CarRequest carRequest = new CarRequest(model, mark, numberPlate, price, description);
        if (tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = carService.addCar(carRequest, images);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @DeleteMapping("delete/{carId}")
    public ResponseEntity<?> deleteCar(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @PathVariable String carId) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)) {
            if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            } else {
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        if (tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = carService.deleteCar(carId);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @PatchMapping("/updatePrice")
    public ResponseEntity<?> updateCarPrice(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @RequestBody CarUpdateRequest carUpdateRequest) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)) {
            if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            } else {
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        if (tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = carService.updateCarPrice(carUpdateRequest);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @GetMapping("/{carId}")
    public ResponseEntity<?> getCar(@PathVariable String carId) {
        return carService.getCar(carId);
    }

    @GetMapping("/carList")
    public ResponseEntity<?> getCarList() {
        return carService.getCarList();
    }

    @GetMapping("/images/{carId}")
    public ResponseEntity < ? > getCarMainImage(@PathVariable String carId) {
        return carService.getCarMainImage(carId);
    }

    @GetMapping("/images/all/{carId}")
    public ResponseEntity<?> getCarAllImageList(@PathVariable String carId) {
        return carService.getCarAllImageList(carId);
    }

    @GetMapping("/images/single/{imageId}")
    public ResponseEntity < ? > getCarImage(@PathVariable String imageId) {
        return carService.getCarImage(imageId);
    }

}
