package wypozyczalnia.example.wypozyczalnia.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Data
@Entity
@Table(name = "token")

public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private String id;
    @Column(name = "user_id", updatable = false)
    private String userID;

    @Column(name = "content", nullable = false)
    private String content;

    public Token() {
    }

    public Token(String userID, String content) {
        this.userID = userID;
        this.content = content;
    }

}
