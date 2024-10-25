package wypozyczalnia.example.wypozyczalnia.demo.services;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import wypozyczalnia.example.wypozyczalnia.demo.DTOs.UserDTO;
import wypozyczalnia.example.wypozyczalnia.demo.entities.User;
import wypozyczalnia.example.wypozyczalnia.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wypozyczalnia.example.wypozyczalnia.demo.repositories.UserRepository;
import wypozyczalnia.example.wypozyczalnia.demo.requests.UserRegistrationRequest;
import wypozyczalnia.example.wypozyczalnia.demo.requests.UserUpdateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {
    private final UserRepository userRepository;
    final String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
*/
   /* public User getUserById(String id) {
        return userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with id [%s] not found".formatted(id))
                );
    }*/

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with email [%s] not found".formatted(email))
                );
    }

   /* public void deleteUser(String id) {
        User user = userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with id [%s] not found".formatted(id))
                );

        userDAO.deleteUser(user);
    }*/

    /*public void updateUser(String id, UserUpdateRequest userUpdateRequest) {
        User user = userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with id [%s] not found".formatted(id))
                );

        if (userUpdateRequest.firstname() != null) {
            String firstname = userUpdateRequest.firstname();
            user.setFirstname(firstname);
        }

        if (userUpdateRequest.lastname() != null) {
            String lastname = userUpdateRequest.lastname();
            user.setLastname(lastname);
        }

        if (userUpdateRequest.email() != null && !user.getEmail().equals(userUpdateRequest.email())) {
            String email = userUpdateRequest.email();
            checkEmailExists(email);
            if (!checkEmailValid(email, "^(.+)@(\\S+)$")) {
                throw new NotValidResourceException("Email not valid");
            }
            user.setEmail(email);
        }

        userDAO.updateUser(user);
    }*/

    public ResponseEntity<?> addUser(UserRegistrationRequest userRegistrationRequest) {

        if(userRegistrationRequest.firstName() == null || userRegistrationRequest.firstName().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing first name");
        }
        if(userRegistrationRequest.lastName() == null || userRegistrationRequest.lastName().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing last name");
        }
        if(userRegistrationRequest.email() == null || userRegistrationRequest.email().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing email");
        }
        if(userRegistrationRequest.password() == null || userRegistrationRequest.password().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing password");
        }
        if(userRegistrationRequest.repPassword() == null || userRegistrationRequest.repPassword().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing repPassword");
        }
        if(userRegistrationRequest.phoneNumber() == null || userRegistrationRequest.phoneNumber().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing phone number");
        }

        String firstname = userRegistrationRequest.firstName();
        String lastname = userRegistrationRequest.lastName();
        String email = userRegistrationRequest.email();
        String password = userRegistrationRequest.password();
        String retPassword = userRegistrationRequest.repPassword();


        if (!checkEmailValid(email, regexPattern) || email.length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepoprawny format email");
        }
        ResponseEntity<?> checkFullNameResult = checkFullName(firstname, lastname);
        if (checkFullNameResult.getStatusCode() != HttpStatus.OK) {
            return checkFullNameResult;
        }
        ResponseEntity<?> passwordValidationResult = passwordValidator(password, retPassword);
        if (passwordValidationResult.getStatusCode() != HttpStatus.OK) {
            return passwordValidationResult;
        }
        ResponseEntity<?> checkEmailExistsResult = checkEmailExists(email);
        if (checkEmailExistsResult.getStatusCode() != HttpStatus.OK) {
            return checkEmailExistsResult;
        }

        //Encrypting password
        String generatedSecuredPasswordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User user = new User(firstname, lastname, email, generatedSecuredPasswordHash, userRegistrationRequest.phoneNumber());
        userRepository.save(user);
        return ResponseEntity.ok("User registered");
    }

    private ResponseEntity<?> passwordValidator(String password, String retPassword) {
        if (password.length() < 8 || password.length() > 32) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hasło powinno mieć między 8 a 32 znaki");
        }
        if (!password.matches(".*[a-z].*")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hasło powinno zawierać przynajmniej jedną małą literę");
        }
        if (!password.matches(".*[A-Z].*")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hasło powinno zawierać przynajmniej jedną dużą literę");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hasło powinno zawierać przynajmniej jeden znak specjalny");
        }
        if (!password.matches(".*\\d.*")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hasło powinno mieć przynajmniej jedną cyfrę");
        }
        if (password.contains(" ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hasło nie może mieć spacji");
        }
        if (!password.equals(retPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Powtórzone hasło niepoprawne");
        }
        return ResponseEntity.ok("Password is approved");
    }

    private ResponseEntity<?> checkFullName(String firstname, String lastname) {
        if (firstname.length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid firstname");
        }
        if (lastname.length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid lastname");
        }
        return ResponseEntity.ok("Fullname checked");
    }

    private ResponseEntity<?> checkFirstName(String firstName) {
        if (firstName.length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid firstName");
        }
        return ResponseEntity.ok("FirstName checked");
    }

    private ResponseEntity<?> checkLastName(String lastName) {
        if (lastName.length() > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid lirstName");
        }
        return ResponseEntity.ok("LastName checked");
    }

    private boolean checkEmailValid(String email, String emailRegex) {
        return Pattern.compile(emailRegex)
                .matcher(email)
                .matches();
    }
    private ResponseEntity<?> checkEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email jest już zajęty");
        }
        return ResponseEntity.ok("Email checked");
    }

    public ResponseEntity<?> getUserData(String userId){
        return ResponseEntity.status(HttpStatus.OK).body(convertToUserDTO(userRepository.getById(userId)));
    }

    private UserDTO convertToUserDTO(User user){
        return new UserDTO(user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getPhoneNumber());
    }

    public ResponseEntity<?> updateUserData(UserUpdateRequest request){
        if(request.firstName() == null && request.lastName() == null && request.email() == null
        && request.password() == null && request.repPassword() == null && request.phoneNumber() == null
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not provided any data");
        }

        Optional<User> userOptional = userRepository.findById(request.userId());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(request.phoneNumber() != null){
                if(request.phoneNumber().length() == 9){
                    user.setPhoneNumber(request.phoneNumber());
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepoprawny numer telefonu");
                }
            }
            if(request.password() != null && request.repPassword() != null){
                ResponseEntity<?> passwordResponseValidator = passwordValidator(request.password(), request.repPassword());
                if(passwordResponseValidator.getStatusCode() == HttpStatus.OK){
                    String generatedSecuredPasswordHash = BCrypt.hashpw(request.password(), BCrypt.gensalt(12));
                    user.setPassword(generatedSecuredPasswordHash);
                } else {
                    return passwordResponseValidator;
                }
            }
            if(request.password() != null && request.repPassword() == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Powtórzenie hasła jest wymagane");
            }
            if(request.password() == null && request.repPassword() != null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Brakuje wpisania hasła");
            }
            if(request.firstName() != null){
                ResponseEntity<?> nameResponseValidation = checkFirstName(request.firstName());
                if(nameResponseValidation.getStatusCode() == HttpStatus.OK){
                    user.setFirstname(request.firstName());
                } else {
                    return nameResponseValidation;
                }
            }
            if(request.lastName() != null){
                ResponseEntity<?> nameResponseValidation = checkLastName(request.lastName());
                if(nameResponseValidation.getStatusCode() == HttpStatus.OK){
                    user.setLastname(request.lastName());
                } else {
                    return nameResponseValidation;
                }
            }
            if(request.email() != null){
                if(checkEmailValid(request.email(), regexPattern)){
                    ResponseEntity<?> emailValidationResponse = checkEmailExists(request.email());
                    if(emailValidationResponse.getStatusCode() == HttpStatus.OK){
                        user.setEmail(request.email());
                    } else {
                        return emailValidationResponse;
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepooprawny email");
                }
            }
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body("Zaaktualizowano");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
        }
    }

    public ResponseEntity<?> getUserList(){
        List<User> userList = userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();
        for(User user : userList){
            userDTOList.add(convertToUserDTO(user));
        }
        return ResponseEntity.status(HttpStatus.OK).body(userDTOList);
    }

    public ResponseEntity<?> addAdminPermisson(String userId){
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            user.setAdmin(true);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body("Zaaktualizowano");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepooprawny user");
        }
    }
}
