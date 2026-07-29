package fab.nkomoelate.taskmanagement.controllers;

import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.services.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                               String email,
                               @NotBlank(message = "Le mot de passe est obligatoire")
                               @Size(min = 8, message = "le mot de passe doit contenir au moins 8 caractères")
                               String password){}

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody CreateUserRequest user){
        User userToCreate = userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userToCreate);
    }
    @GetMapping("{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id){
        User user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }
    @GetMapping
    public ResponseEntity<List<User>> getUsers(){
        List<User> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }
    @PutMapping("{id}")
    public ResponseEntity<User> updateUser(@Valid @RequestBody CreateUserRequest user, @PathVariable Long id){
        User userToUpdate = userService.updateUser(user,id);
        return ResponseEntity.ok(userToUpdate);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
