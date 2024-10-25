package wypozyczalnia.example.wypozyczalnia.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wypozyczalnia.example.wypozyczalnia.demo.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}

