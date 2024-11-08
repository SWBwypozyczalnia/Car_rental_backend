package wypozyczalnia.example.wypozyczalnia.demo.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wypozyczalnia.example.wypozyczalnia.demo.DTOs.LoginDTO;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Token;
import wypozyczalnia.example.wypozyczalnia.demo.entities.User;
import wypozyczalnia.example.wypozyczalnia.demo.exception.NotValidResourceException;
import wypozyczalnia.example.wypozyczalnia.demo.exception.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import wypozyczalnia.example.wypozyczalnia.demo.repositories.TokenRepository;
import wypozyczalnia.example.wypozyczalnia.demo.repositories.UserRepository;

import java.security.Key;
import java.util.Date;

@Service
public class TokenService {
    private static final long EXPIRATION_TIME_ACCESS = 900000 / 15;
    private static final long EXPIRATION_TIME_REFRESH = 3600000 * 24;
    private final Key jwtKey;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(@Value("${jwt.Key}") String secretKey,
                        UserRepository userRepository, TokenRepository tokenRepository) {
        this.jwtKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    public void addToken(String userId, String tokenString) {
        if (tokenString == null) {
            throw new NotValidResourceException("Missing data");
        }
        Token token = new Token(userId, tokenString);
        tokenRepository.save(token);
    }

    public Token getTokenByContent(String token) {
        return tokenRepository.findByContent(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Token [%s] not found".formatted(token))
                );
    }

    public String generateToken(long expirationDate, String userID) {
        long currentTimeMillis = System.currentTimeMillis();
        Date expirationDateToken = new Date(currentTimeMillis + expirationDate);
        String token = Jwts.builder()
                .setSubject(userID)
                .claim("admin", userRepository.getById(userID).isAdmin())
                .setExpiration(expirationDateToken)
                .signWith(jwtKey, SignatureAlgorithm.HS512)
                .compact();
        return token;
    }

    public ResponseEntity<?> loginUser(String email, String password, UserService userService) {
        try {
            User user = userService.getUserByEmail(email);
            // checking password
            if (BCrypt.checkpw(password, user.getPassword())) {
                // generating tokens
                String accessToken = generateToken(EXPIRATION_TIME_ACCESS, user.getId());
                String refreshToken = generateToken(EXPIRATION_TIME_REFRESH, user.getId());
                // adding refresh token to database
                addToken(user.getId(), refreshToken);
                // adding refresh token to cookies
                Cookie cookie = new Cookie("refreshToken", refreshToken);
                cookie.setHttpOnly(false);
                cookie.setPath("/");
                cookie.setMaxAge(60 * 60 * 24); // 1 dzień
                cookie.setSecure(false); // Na lokalne testy, na produkcji ustaw na true
                String cookieValue = String.format("%s=%s; HttpOnly; Path=/; SameSite=Lax", cookie.getName(), cookie.getValue());

                Cookie cookieAccess = new Cookie("accessToken", accessToken);
                cookieAccess.setHttpOnly(false);
                cookieAccess.setPath("/");
                cookieAccess.setMaxAge(60 * 60 * 24); // 1 dzień
                cookieAccess.setSecure(false); // Na lokalne testy, na produkcji ustaw na true
                String cookieValueAccess = String.format("%s=%s; HttpOnly; Path=/; SameSite=Lax", cookieAccess.getName(), cookieAccess.getValue());

                LoginDTO loginDTO = new LoginDTO(user.getId(), user.isAdmin());
                return ResponseEntity.ok()
                        .header("Set-Cookie", cookieValue)
                        .header("Set-Cookie", cookieValueAccess)
                        .body(loginDTO);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepoprawne hasło");
            }
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepoprawny email");
        }
    }

    public ResponseEntity<?> refreshToken(String refreshToken) {
        if (!refreshToken.isEmpty()) {
            try {
                Claims claims = Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(refreshToken).getBody();
                // getting userID from refresh token
                String userID = claims.getSubject();
                // getting refresh token from database
                Token dataBaseRefreshToken = getTokenByContent(refreshToken);
                // checking refresh token
                if (dataBaseRefreshToken.getContent().equals(refreshToken) && dataBaseRefreshToken.getUserID().equals(userID)) {
                    // Creating new access token
                    String accessToken = generateToken(EXPIRATION_TIME_ACCESS, userID);
                    return ResponseEntity.ok().body(accessToken);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
                }
            } catch (ExpiredJwtException e) {
                // token inactive
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired token");
            } catch (ResourceNotFoundException e) {
                // token not in database
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token not in database");
            }
        } else {
            // Cookie refreshToken not found
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Empty cookie");
        }
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(token).getBody();
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    @Transactional
    public void deleteToken(String tokenContent) {
        tokenRepository.deleteByContent(tokenContent);
    }

    public ResponseEntity<?> checkLogged(String accessToken, String refreshToken) {
        if(accessToken.isBlank() || accessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing access token");
        }

        try {
            if (validateToken(accessToken)) {
                return ResponseEntity.ok("User logged");
            } else {
                ResponseEntity<?> refreshTokenResponse = refreshToken(refreshToken);
                if(!refreshTokenResponse.getStatusCode().equals(HttpStatus.OK)){
                    return refreshTokenResponse;
                }
                String newAccessToken = refreshTokenResponse.getBody().toString();
                return ResponseEntity.status(HttpStatus.CREATED).body(newAccessToken);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error parsing token");
        }
    }

    public String getUserIDFromAccessToken(String accessToken) {
        if (validateToken(accessToken)) {
            return getUserIdFromToken(accessToken);
        }
        return null;
    }

    public Boolean checkIfAdminFromAccessToken(String accessToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(accessToken).getBody();
        return (Boolean) claims.get("admin");
    }

    public ResponseEntity<?> addNewCookieToResponse(String newAccessToken, ResponseEntity<?> endpointResult) {
        String newCookie = createCookie(newAccessToken);
        return ResponseEntity.status(endpointResult.getStatusCode())
                .header("Set-Cookie", newCookie)
                .body(endpointResult.getBody());
    }

    public String createCookie(String cookieValue){
        Cookie newCookie = new Cookie("accessToken", cookieValue);
        newCookie.setHttpOnly(true);
        newCookie.setPath("");
        return String.format("%s=%s; HttpOnly; Path=/", newCookie.getName(), newCookie.getValue());
    }
}
