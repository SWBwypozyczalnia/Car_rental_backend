package wypozyczalnia.example.wypozyczalnia.demo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wypozyczalnia.example.wypozyczalnia.demo.requests.GetReservationsCarRequest;
import wypozyczalnia.example.wypozyczalnia.demo.requests.GetReservationsUserRequest;
import wypozyczalnia.example.wypozyczalnia.demo.requests.ReservationRequest;
import wypozyczalnia.example.wypozyczalnia.demo.requests.ReservationUpdateRequest;
import wypozyczalnia.example.wypozyczalnia.demo.services.ReservationService;
import wypozyczalnia.example.wypozyczalnia.demo.services.TokenService;

import java.time.LocalDate;

@RestController
@RequestMapping("api/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final TokenService tokenService;

    public ReservationController(ReservationService reservationService, TokenService tokenService){
        this.reservationService = reservationService;
        this.tokenService = tokenService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addReservationLoggedUser(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @RequestBody ReservationRequest requestBody) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        String loggedUserId = tokenService.getUserIDFromAccessToken(accessToken);

        if (loggedUserId.equals(requestBody.userId()) || tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = reservationService.addReservation(requestBody);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @PatchMapping("/pickup/{reservationId}")
    public ResponseEntity<?> pickupReservation(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @PathVariable String reservationId) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        if(tokenService.checkIfAdminFromAccessToken(accessToken)){
            ResponseEntity<?> response = reservationService.pickupReservation(reservationId);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @PatchMapping("/return/{reservationId}")
    public ResponseEntity<?> returnReservation(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @PathVariable String reservationId) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        if(tokenService.checkIfAdminFromAccessToken(accessToken)){
            ResponseEntity<?> response = reservationService.returnReservation(reservationId);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getReservation(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @PathVariable String reservationId) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        String loggedUserId = tokenService.getUserIDFromAccessToken(accessToken);

        ResponseEntity<?> response = reservationService.getReservation(reservationId, loggedUserId, accessToken);

        if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
            return tokenService.addNewCookieToResponse(accessToken, response);
        }
        return response;

    }

    @GetMapping("/car/{carId}")
    public ResponseEntity<?> getReservationByCarId(@PathVariable String carId,
                                                   @RequestParam String after) {
        return reservationService.getReservationByCarId(carId, after);
    }

    @GetMapping("/car")
    public ResponseEntity<?> getReservationByCarIdAfterDate(
            @RequestParam("carId") String carId,
            @RequestParam("date") LocalDate date) {
        return reservationService.getReservationByCarIdAfterDate(carId, date);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReservationByUserId(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @PathVariable String userId){

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        String loggedUserId = tokenService.getUserIDFromAccessToken(accessToken);

        if (loggedUserId.equals(userId) || tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = reservationService.getReservationByUserId(userId);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getReservationByUserIdInDates(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @RequestBody GetReservationsUserRequest request){

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        String loggedUserId = tokenService.getUserIDFromAccessToken(accessToken);

        if (loggedUserId.equals(request.userId()) || tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = reservationService.getReservationByUserIdInDates(request);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @GetMapping("/reservationList")
    public ResponseEntity<?> getReservationList(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        if (tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = reservationService.getReservationList();

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @DeleteMapping("/delete/{reservationId}")
    public ResponseEntity<?> deleteReservation(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @PathVariable String reservationId) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        ResponseEntity<?> response = reservationService.deleteReservation(reservationId, accessToken);

        if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
            return tokenService.addNewCookieToResponse(accessToken, response);
        }
        return response;
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateReservation(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @RequestBody ReservationUpdateRequest request) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        ResponseEntity<?> response = reservationService.updateReservation(request, accessToken);

        if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
            return tokenService.addNewCookieToResponse(accessToken, response);
        }
        return response;
    }
}
