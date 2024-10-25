package wypozyczalnia.example.wypozyczalnia.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wypozyczalnia.example.wypozyczalnia.demo.entities.Token;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {
    void deleteByContent(String tokenContent);
    void deleteAllByUserID(String UserID);
    Optional<Token> findByContent(String token);
}
