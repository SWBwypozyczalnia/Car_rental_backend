package wypozyczalnia.example.wypozyczalnia.demo.user;

public record CustomerUpdateRequest(
        String firstname, String lastname, String email, String address, String phoneNumber
) {
}
