package wypozyczalnia.example.wypozyczalnia.demo.services;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wypozyczalnia.example.wypozyczalnia.demo.DTOs.*;
import wypozyczalnia.example.wypozyczalnia.demo.entities.*;
import wypozyczalnia.example.wypozyczalnia.demo.repositories.*;
import wypozyczalnia.example.wypozyczalnia.demo.requests.*;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final CarImageRepository carImageRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public CarService(CarRepository carRepository, CarImageRepository carImageRepository, ReservationRepository reservationRepository) {
        this.carRepository = carRepository;
        this.carImageRepository = carImageRepository;
        this.reservationRepository = reservationRepository;
    }

    public ResponseEntity<?> addCar(CarRequest carRequest, List<MultipartFile> images) {
        // Sprawdzenie, czy pola nie sa nullami badz nie sa puste
        if(carRequest.mark() == null || carRequest.mark().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing mark");
        }
        if(carRequest.model() == null || carRequest.model().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing model");
        }
        if(carRequest.numberPlate() == null || carRequest.numberPlate().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing number plate");
        }
        if(carRequest.description()== null || carRequest.description().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing description");
        }
        // Sprawdzenie, czy samochód o danej tablicy rejestracyjnej już istnieje
        Optional<Car> existingCar = carRepository.findByNumberPlate(carRequest.numberPlate());
        if (existingCar.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Car with this number plate already exists");
        }
        Car car = new Car();
        // Sprawdzenie czy cena jest dodatnia
        if(carRequest.price() < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Price must be greater than 0");
        }
        car.setModel(carRequest.model());
        car.setMark(carRequest.mark());
        car.setNumberPlate(carRequest.numberPlate());
        car.setPrice(carRequest.price());
        car.setDescription(carRequest.description());

        List <CarImages> carImagesList = new ArrayList < > ();
        carRepository.save(car);


        if (images != null && !images.isEmpty()) {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            String bucketName = "car-images-bucket-132";
            boolean isFirstImage = true;
            for (MultipartFile file: images) {
                try {
                    //String baseResourcesPath = Paths.get("src", "main", "resources", "images", car.getId()).toString();
                    //Files.createDirectories(Paths.get(baseResourcesPath));
                    //String imagePath = baseResourcesPath + "/" + file.getOriginalFilename();

                    String objectName = "images/" + car.getId() + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

                    //to zapisuje ale do tmp
                    //String tempDir = System.getProperty("java.io.tmpdir");
                    //String baseResourcesPath = Paths.get(tempDir, "images", car.getId().toString()).toString();
                    //Files.createDirectories(Paths.get(baseResourcesPath));
                    //String imagePath = Paths.get(baseResourcesPath, file.getOriginalFilename()).toString();
                    //Files.copy(file.getInputStream(), Paths.get(imagePath), StandardCopyOption.REPLACE_EXISTING);

                    // Utwórz BlobId i BlobInfo
                    BlobId blobId = BlobId.of(bucketName, objectName);
                    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
                    // Zapisz plik do Google Cloud Storage
                    storage.create(blobInfo, file.getBytes());

                    String fileUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);

                    CarImages carImage = new CarImages();
                    carImage.setCar(car);
                    carImage.setImagePath(fileUrl);
                    carImage.setMainPicture(isFirstImage); // Ustawienie mainPicture na true tylko dla pierwszego obrazka
                    isFirstImage = false;
                    carImageRepository.save(carImage);
                    carImagesList.add(carImage);
                } catch (Exception e) {

                    return new ResponseEntity < > ("Error saving images", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        car.setImages(carImagesList);
        carRepository.save(car);
        return ResponseEntity.status(HttpStatus.OK).body("Car added");
    }

    public ResponseEntity<?> deleteCar(String carId) {
        Optional<Car> carOptional = carRepository.findById(carId);
        if (carOptional.isPresent()) {
            Car car = carOptional.get();
            List<Reservation> reservations = reservationRepository.findAllByCarId(carId);
            if (!reservations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete car with active reservations");
            }
            carRepository.delete(car);
            return ResponseEntity.status(HttpStatus.OK).body("Car deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
        }
    }

    public ResponseEntity<?> updateCarPrice(CarUpdateRequest carUpdateRequest) {
        // Sprawdzenie czy cena jest dodatnia
        Optional<Car> carOptional = carRepository.findById(carUpdateRequest.carId());
        if(carUpdateRequest.price() < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Price must be greater than 0");
        }
        if (carOptional.isPresent()) {
            Car car = carOptional.get();
            car.setPrice(carUpdateRequest.price());
            carRepository.save(car);
            return ResponseEntity.status(HttpStatus.OK).body("Car price updated");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
        }
    }

    public ResponseEntity<?> getCar(String carId) {
        if (carId == null || carId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing carId");
        }
        Optional<Car> carOptional = carRepository.findById(carId);
        if (carOptional.isPresent()) {
            Car car = carOptional.get();
            return ResponseEntity.status(HttpStatus.OK).body(new CarDTO(car));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
        }
    }

    public ResponseEntity<?> getCarList(){
        List<Car> carList = carRepository.findAll();
        List<CarDTO> carDTOList = new ArrayList<>();
        for(Car car : carList){
            carDTOList.add(new CarDTO(car));
        }
        return ResponseEntity.status(HttpStatus.OK).body(carDTOList);
    }

    public ResponseEntity<?> getCarMainImage(String carId) {
        Optional<Car> carOptional = carRepository.findById(carId);
        if (carOptional.isPresent()) {
            Car car = carOptional.get();
            Optional<CarImages> mainImageOptional = car.getImages().stream()
                    .filter(CarImages::getMainPicture)
                    .findFirst();

            if (mainImageOptional.isPresent()) {
                CarImages mainImage = mainImageOptional.get();
                String imagePath = mainImage.getImagePath();
                try {
                    // Pobierz nazwę bucketa i obiekt
                    String bucketName = "car-images-bucket-132";
                    String objectName = imagePath.replace(
                            String.format("https://storage.googleapis.com/%s/", bucketName), ""
                    );

                    // Inicjalizuj klienta Google Cloud Storage
                    Storage storage = StorageOptions.getDefaultInstance().getService();

                    // Pobierz dane pliku jako bajty
                    Blob blob = storage.get(BlobId.of(bucketName, objectName));
                    if (blob == null) {
                        return new ResponseEntity<>("Image not found in storage", HttpStatus.NOT_FOUND);
                    }
                    byte[] imageBytes = blob.getContent();

                    // Przygotuj odpowiedź z obrazkiem
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(blob.getContentType()));
                    return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
                } catch (Exception e) {
                    return new ResponseEntity<>("Error reading image from storage: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>("Main image not found", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("Car not found", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getCarImage(String imageId) {
        Optional<CarImages> carImagesOptional = carImageRepository.findById(imageId);
        if (carImagesOptional.isPresent()) {
            CarImages carImage = carImagesOptional.get();
            Path imagePath = Path.of(carImage.getImagePath());
            try {
                byte[] imageBytes = Files.readAllBytes(imagePath);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            } catch (IOException e) {
                return new ResponseEntity<>("Error reading image file", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Car not found", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getCarAllImageList(String carId) {
        Optional<Car> carOptional = carRepository.findById(carId);
        if (carOptional.isPresent()) {
            Car car = carOptional.get();
            List<CarImages> images = car.getImages();

            List<byte[]> imageBytesList = new ArrayList<>();
            for (CarImages image : images) {
                Path imagePath = Path.of(image.getImagePath());
                try {
                    byte[] imageBytes = Files.readAllBytes(imagePath);
                    imageBytesList.add(imageBytes);
                } catch (IOException e) {
                    return new ResponseEntity<>("Error reading image file", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(imageBytesList, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Car not found", HttpStatus.NOT_FOUND);
        }
    }

}
