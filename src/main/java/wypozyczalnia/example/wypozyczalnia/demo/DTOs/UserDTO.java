package wypozyczalnia.example.wypozyczalnia.demo.DTOs;

import jakarta.persistence.Column;
import lombok.Data;
import wypozyczalnia.example.wypozyczalnia.demo.entities.User;

import java.time.LocalDate;

@Data
public class UserDTO {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;

    public UserDTO(String id, String firstname, String lastname, String email, String phoneNumber) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
    }
}
