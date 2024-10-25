package wypozyczalnia.example.wypozyczalnia.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import wypozyczalnia.example.wypozyczalnia.demo.DTOs.CarDTO;
import wypozyczalnia.example.wypozyczalnia.demo.DTOs.ReservationDTO;
import wypozyczalnia.example.wypozyczalnia.demo.DTOs.ReservationListDTO;
import wypozyczalnia.example.wypozyczalnia.demo.DTOs.UserDTO;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Car;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Reservation;
import wypozyczalnia.example.wypozyczalnia.demo.entities.User;
import wypozyczalnia.example.wypozyczalnia.demo.repositories.CarRepository;
import wypozyczalnia.example.wypozyczalnia.demo.repositories.ReservationRepository;
import wypozyczalnia.example.wypozyczalnia.demo.repositories.UserRepository;
import wypozyczalnia.example.wypozyczalnia.demo.requests.GetReservationsCarRequest;
import wypozyczalnia.example.wypozyczalnia.demo.requests.GetReservationsUserRequest;
import wypozyczalnia.example.wypozyczalnia.demo.requests.ReservationRequest;
import wypozyczalnia.example.wypozyczalnia.demo.requests.ReservationUpdateRequest;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class ReservationService {
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final TokenService tokenService;
    private final UserService userService;

    @Autowired
    public ReservationService(CarRepository carRepository, UserRepository userRepository,
                              ReservationRepository reservationRepository, TokenService tokenService, UserService userService) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    public ResponseEntity<?> addReservation(ReservationRequest reservationRequest) {
        ResponseEntity<?> validateDataResponse = validateReservationData(reservationRequest);
        if(validateDataResponse.getStatusCode() != HttpStatus.OK) {
            return validateDataResponse;
        }
        Reservation reservation = new Reservation();
        Optional<Car> car = carRepository.findById(reservationRequest.carId());
        if(car.isPresent()) {
            reservation.setCar(car.get());
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
        }
        Optional<User> user = userRepository.findById(reservationRequest.userId());
        if(user.isPresent()) {
            reservation.setUser(user.get());
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // sprawdzanie czy nie ma innych rezerwacji
        List<Reservation> conflictingReservations = reservationRepository.findByCarIdAndEndDateGreaterThanEqualAndStartingDateLessThanEqual(
                reservationRequest.carId(), reservationRequest.startingDate(), reservationRequest.endDate());
        if (!conflictingReservations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Car is not available for the specified dates");
        }

        // Sprawdzenie czy rezerwacja trwa maksymalnie 7 dni
        long numberOfDays = ChronoUnit.DAYS.between(reservationRequest.startingDate(), reservationRequest.endDate());
        if (numberOfDays > 7) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reservation duration cannot exceed 7 days");
        }

        // Sprawdzenie czy data zakończenia jest po dacie rozpoczęcia
        if (reservationRequest.endDate().isBefore(reservationRequest.startingDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End date must be after start date");
        }

        reservation.setEndDate(reservationRequest.endDate());
        reservation.setStartingDate(reservationRequest.startingDate());
        reservation.setReservatinDate(reservationRequest.reservationDate());
        reservation.setPickedUp(false);
        reservation.setReturned(false);

        reservationRepository.save(reservation);
        return ResponseEntity.status(HttpStatus.OK).body("Reservation added");
    }

    public ResponseEntity<?> validateReservationData(ReservationRequest request) {
        if(request.userId() == null || request.userId().isBlank()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing userId");
        }
        if(request.carId() == null || request.carId().isBlank()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing carId");
        }
        if(request.reservationDate() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing reservationDate");
        }
        if(request.startingDate() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing startingDate");
        }
        if(request.endDate() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing endDate");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Valid data");
    }

    public ResponseEntity<?> getReservation(String reservationId, String loggedUserId, String accessToken) {
        if(loggedUserId == null || loggedUserId.isBlank()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing loggedUserId");
        }
        if(reservationId == null || reservationId.isBlank()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing reservationId");
        }
        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
        if(reservationOptional.isPresent()){
            Reservation reservation = reservationOptional.get();
            if(reservation.getUser().getId().equals(loggedUserId) ||
                    tokenService.checkIfAdminFromAccessToken(accessToken)) {
                UserDTO userDTO = new UserDTO(reservation.getUser());
                CarDTO carDTO = new CarDTO(reservation.getCar());
                return ResponseEntity.status(HttpStatus.OK).body(new ReservationDTO(reservation, userDTO, carDTO));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }
    }

    public ResponseEntity<?> getReservationByCarId(String carId, String after) {
        Optional<Car> car = carRepository.findById(carId);
        if(car.isPresent()){
            List<Reservation> carReservations = reservationRepository.findAllByCarId(carId);
            return ResponseEntity.status(HttpStatus.OK).body(getTenReservations(carReservations, after));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
        }
    }
    public ResponseEntity<?> getReservationByCarIdAfterDate(String carId, LocalDate startDate) {
        if(carId.isBlank() || carId.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing carId");
        }
        if(startDate == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing startDate");
        }

        Optional<Car> car = carRepository.findById(carId);
        if(car.isPresent()){
            List<Reservation> carReservations =
                    reservationRepository.findByCarIdAndStartingDateGreaterThanEqual(
                            carId, startDate);
            List<ReservationDTO> reservationDTOS = new ArrayList<>();
            for (Reservation reservation : carReservations) {
                reservationDTOS.add(new ReservationDTO(reservation, new UserDTO(reservation.getUser()), new CarDTO(reservation.getCar())));
            }
            return ResponseEntity.status(HttpStatus.OK).body(reservationDTOS);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
        }
    }

    public ResponseEntity<?> getReservationByUserId(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            List<Reservation> userReservations = reservationRepository.findAllByUserId(userId);
            List<ReservationDTO> reservationDTOS = new ArrayList<>();
            for (Reservation reservation : userReservations) {
                reservationDTOS.add(new ReservationDTO(reservation, new UserDTO(reservation.getUser()), new CarDTO(reservation.getCar())));
            }
            return ResponseEntity.status(HttpStatus.OK).body(reservationDTOS);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    public ResponseEntity<?> getReservationByUserIdInDates(GetReservationsUserRequest request) {
        if(request.userId().isBlank() || request.userId().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing carId");
        }
        if(request.startDate() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing startDate");
        }
        if(request.endDate() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing endDate");
        }
        Optional<User> user = userRepository.findById(request.userId());
        if(user.isPresent()){
            List<Reservation> carReservations =
                    reservationRepository.findByUserIdAndEndDateGreaterThanEqualAndStartingDateLessThanEqual(
                            request.userId(), request.startDate(), request.endDate());
            return ResponseEntity.status(HttpStatus.OK).body(getTenReservations(carReservations, request.after()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    public ReservationListDTO getTenReservations(List<Reservation> reservations, String after){
        boolean lastPage = false;
        reservations.sort(Comparator.comparing(Reservation::getEndDate).reversed());

        if (after != null) {
            List <Reservation> finalReservations = reservations;
            int index = IntStream.range(0, reservations.size())
                    .filter(i -> finalReservations.get(i).getId().equals(after))
                    .findFirst()
                    .orElse(-1);
            if (index != -1) {
                index++;
                if (index >= reservations.size()) {
                    reservations = Collections.emptyList();
                    lastPage = true;
                } else {
                    reservations = reservations.subList(index, reservations.size());
                }
            }
        }

        if(reservations.size() < 10){
            lastPage = true;
        } else {
            reservations = reservations.subList(0, 10);
        }

        List<ReservationDTO> reservationDTOS = new ArrayList<>();
        for (Reservation reservation : reservations) {
            reservationDTOS.add(new ReservationDTO(reservation, new UserDTO(reservation.getUser()), new CarDTO(reservation.getCar())));
        }
        return new ReservationListDTO(reservationDTOS, lastPage);
    }

    public ResponseEntity<?> pickupReservation(String reservationId){
        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
        if(reservationOptional.isPresent()){
            Reservation reservation = reservationOptional.get();
            reservation.setPickedUp(!reservation.isPickedUp());
            reservationRepository.save(reservation);
            return ResponseEntity.status(HttpStatus.OK).body("Reservation edited");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }
    }

    public ResponseEntity<?> returnReservation(String reservationId){
        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
        if(reservationOptional.isPresent()){
            Reservation reservation = reservationOptional.get();
            reservation.setReturned(!reservation.isReturned());
            reservationRepository.save(reservation);
            return ResponseEntity.status(HttpStatus.OK).body("Reservation edited");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }
    }

    public ResponseEntity<?> getReservationList(){
        List<Reservation> reservationList = reservationRepository.findAll();
        List<ReservationDTO> reservationDTOList = new ArrayList<>();
        for(Reservation reservation : reservationList){
            reservationDTOList.add(new ReservationDTO(reservation, new UserDTO(reservation.getUser()), new CarDTO(reservation.getCar())));
        }
        return ResponseEntity.status(HttpStatus.OK).body(reservationDTOList);
    }

    public ResponseEntity<?> deleteReservation(String reservationId, String accessToken){
        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
        if(reservationOptional.isPresent()){
            Reservation reservation = reservationOptional.get();
            String loggedUserId = tokenService.getUserIDFromAccessToken(accessToken);
            if(reservation.getUser().getId().equals(loggedUserId) || tokenService.checkIfAdminFromAccessToken(accessToken)) {
                reservationRepository.delete(reservation);
                return ResponseEntity.status(HttpStatus.OK).body("Reservation deleted");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unauthorized");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }
    }

    public ResponseEntity<?> updateReservation(ReservationUpdateRequest request, String accessToken){
        Optional<Reservation> reservationOptional = reservationRepository.findById(request.reservationId());
        if(reservationOptional.isPresent()){
            Reservation reservation = reservationOptional.get();
            String loggedUserId = tokenService.getUserIDFromAccessToken(accessToken);
            if (!(reservation.getUser().getId().equals(loggedUserId) || tokenService.checkIfAdminFromAccessToken(accessToken))){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            if(reservation.isPickedUp()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Car is already rented");
            }
            if(reservation.isReturned()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Car is already returned");
            }
            Car car = reservation.getCar();
            // sprawdzanie czy nie ma innych rezerwacji
            List<Reservation> conflictingReservations = reservationRepository.findByCarIdAndEndDateGreaterThanEqualAndStartingDateLessThanEqual(
                    car.getId(), request.startingDate(), request.endDate());
            conflictingReservations.removeIf(conflictingReservation -> conflictingReservation.getId().equals(reservation.getId()));
            if (!conflictingReservations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Car is not available for the specified dates");
            }

            // Sprawdzenie czy data zakończenia jest po dacie rozpoczęcia
            if (request.endDate().isBefore(request.startingDate())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End date must be after start date");
            }

            // Sprawdzenie czy rezerwacja trwa maksymalnie 7 dni
            long numberOfDays = ChronoUnit.DAYS.between(request.startingDate(), request.endDate());
            if (numberOfDays > 7) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reservation duration cannot exceed 7 days");
            }

            reservation.setEndDate(request.endDate());
            reservation.setStartingDate(request.startingDate());

            reservationRepository.save(reservation);
            return ResponseEntity.status(HttpStatus.OK).body("Reservation updated");

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }
    }
}
