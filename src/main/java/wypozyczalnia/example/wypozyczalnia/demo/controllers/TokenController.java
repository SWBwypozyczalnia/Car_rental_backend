package wypozyczalnia.example.wypozyczalnia.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wypozyczalnia.example.wypozyczalnia.demo.entities.User;
import wypozyczalnia.example.wypozyczalnia.demo.requests.LoginRequest;
import wypozyczalnia.example.wypozyczalnia.demo.services.TokenService;
import wypozyczalnia.example.wypozyczalnia.demo.services.UserService;

@RestController
@RequestMapping("api/auth")
public class TokenController {
    private final UserService userService;
    private final TokenService tokenService;

    public TokenController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    public User getUserByEmail(@PathVariable("email") String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequest requestBody) {
        if((requestBody.email()==null && requestBody.password()==null)||(requestBody.email()=="" && requestBody.password()=="")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing e-mail and password");
        } else if(requestBody.email()==null || requestBody.email()==""){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing e-mail");
        } else if(requestBody.password() == null || requestBody.password() == ""){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing password");
        } else {
            return tokenService.loginUser(requestBody.email(), requestBody.password(), userService);
        }
    }

    /*@GetMapping("refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        if(refreshToken == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token");
        }else{
            return tokenService.refreshToken(refreshToken);
        }
    }*/

    @DeleteMapping("logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        if (!refreshToken.isEmpty()) {
            try {
                tokenService.deleteToken(refreshToken);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during logout");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing cookie");
        }
    }
}
