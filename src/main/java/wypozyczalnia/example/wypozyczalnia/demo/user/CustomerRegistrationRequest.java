package wypozyczalnia.example.wypozyczalnia.demo.user;

public record CustomerRegistrationRequest(
        String firstname, String lastname, String email, String address, String phoneNumber
) {
}
