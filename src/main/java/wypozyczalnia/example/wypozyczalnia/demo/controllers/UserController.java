package wypozyczalnia.example.wypozyczalnia.demo.controllers;

import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import wypozyczalnia.example.wypozyczalnia.demo.requests.UserRegistrationRequest;
import wypozyczalnia.example.wypozyczalnia.demo.requests.UserUpdateRequest;
import wypozyczalnia.example.wypozyczalnia.demo.services.TokenService;
import wypozyczalnia.example.wypozyczalnia.demo.services.UserService;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("register")
    public ResponseEntity<?> addUser(@RequestBody UserRegistrationRequest request) {
        return userService.addUser(request);
    }

    @GetMapping()
    ResponseEntity<?> getLoggedUserData(
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

        String loggedUserId = tokenService.getUserIDFromAccessToken(accessToken);
        ResponseEntity<?> response = userService.getUserData(loggedUserId);

        if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
            return tokenService.addNewCookieToResponse(accessToken, response);
        }
        return response;
    }

    @GetMapping("/{userId}")
    ResponseEntity<?> getUserData(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @PathVariable String userId) {

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
            ResponseEntity<?> response = userService.getUserData(userId);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUser(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @RequestBody UserUpdateRequest request) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        if(request.userId().isEmpty() || request.userId().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing user id");
        }

        String loggedUserId = tokenService.getUserIDFromAccessToken(accessToken);
        if (loggedUserId.equals(request.userId()) || tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = userService.updateUserData(request);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

    }

    @GetMapping("/userList")
    public ResponseEntity<?> getUserList(
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
            ResponseEntity<?> response = userService.getUserList();

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @PatchMapping("/admin/{userId}")
    public ResponseEntity<?> giveAdminPermisson(
            @CookieValue(value = "accessToken", defaultValue = "") String accessToken,
            @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken,
            @PathVariable String userId) {

        ResponseEntity<?> checkAuthorizationResult = tokenService.checkLogged(accessToken, refreshToken);
        if (!checkAuthorizationResult.getStatusCode().equals(HttpStatus.OK)){
            if(!checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return checkAuthorizationResult;
            }else{
                accessToken = checkAuthorizationResult.getBody().toString();
            }
        }

        if (tokenService.checkIfAdminFromAccessToken(accessToken)) {
            ResponseEntity<?> response = userService.addAdminPermisson(userId);

            if (checkAuthorizationResult.getStatusCode().equals(HttpStatus.CREATED)) {
                return tokenService.addNewCookieToResponse(accessToken, response);
            }
            return response;

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }



}