package fab.nkomoelate.taskmanagement.controllers;

import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.services.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/")
public class UserController {

    private final UserService userService;

    public record CreateUserRequest(
                               String lastName,
                               String  firstName,
                               @NotBlank(message="l'email est obligatoire")
                               @Email(message="Format d'email invalide")
                               String email){}

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody CreateUserRequest user){
        User userToCreate = userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userToCreate);
    }
}
