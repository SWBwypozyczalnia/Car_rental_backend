package wypozyczalnia.example.wypozyczalnia.demo.DTOs;

import lombok.Data;

@Data
public class LoginDTO {
    private String userId;
    private Boolean admin;

    public LoginDTO(String userId, Boolean admin) {
        this.admin = admin;
        this.userId = userId;
    }

}
