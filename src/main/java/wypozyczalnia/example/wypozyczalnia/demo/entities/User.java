package wypozyczalnia.example.wypozyczalnia.demo.entities;

import jakarta.persistence.*;


import java.time.LocalDate;
import lombok.*;
import java.util.Objects;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(
                name = "user_email_unique",
                columnNames = "email"
        )
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @Column(name = "admin", nullable = false)
    private boolean admin;

    public User(String firstname, String lastname, String email, String password, String phoneNumber) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.admin = false;
    }

}
