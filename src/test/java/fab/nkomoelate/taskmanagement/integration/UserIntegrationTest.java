package fab.nkomoelate.taskmanagement.integration;
import fab.nkomoelate.taskmanagement.controllers.UserController.*;
import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import fab.nkomoelate.taskmanagement.security.AuthResponse;
import fab.nkomoelate.taskmanagement.security.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AutoConfigureRestTestClient // ← annotation pour RestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportTestcontainers(TestContainersConfig.class)
public class UserIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        // Register + récupère le token
        AuthResponse auth = restTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterRequest("Test", "User", "test@test.com", "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        // Configure le client avec le token
        restTestClient = restTestClient.mutate()
                .defaultHeader("Authorization", "Bearer " + auth.token())
                .build();
    }
    @Test
    public void createUser_should_persist_an_user_in_database(){
        //given
        CreateUserRequest userRequest = new CreateUserRequest("Fabienne","ELATE LEA","fabienneelatelea@gmail.com","password");
        //when- vraie requete http
        restTestClient.post()
                .uri("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(userRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .value(user -> {
                    assertThat(user.getEmail()).isEqualTo("fabienneelatelea@gmail.com");
                });
    }

    @Test
    void createUser_should_return_409_when_email_already_exists() {
        // Given — on insère un premier user
        CreateUserRequest firstRequest = new CreateUserRequest("Favie", "Flavien", "fabienneelatelea@gmail.com","password");
        restTestClient.post()
                .uri("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(firstRequest)
                .exchange()
                .expectStatus().isCreated();

        // When — on tente d'insérer un second avec le même email
        CreateUserRequest duplicateRequest = new CreateUserRequest("Dupont", "Marie", "fabienneelatelea@gmail.com","password");
        restTestClient.post()
                .uri("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(duplicateRequest)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void getUser_should_return_user_when_found(){
        // Given — on insère un premier user
        CreateUserRequest firstRequest = new CreateUserRequest("Favie", "Flavien", "fabienneelatelea@gmail.com","password");
        User createdUser =restTestClient.post()
                .uri("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(firstRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .returnResult()
                .getResponseBody();

        // Vérification anti-NullPointerException
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        //when
        restTestClient.get()
                .uri("/api/users/{id}", createdUser.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assertThat(user.getEmail()).isEqualTo("fabienneelatelea@gmail.com");
                });
    }

    @Test
    public void getUser_should_return_404_when_user_not_found(){
        restTestClient.get()
                .uri("/api/users/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateUser_should_update_user_in_database(){
        // Given — on insère un premier user
        CreateUserRequest firstRequest = new CreateUserRequest("Favie", "Flavien", "fabienneelatelea@gmail.com","password");
        User createdUser =restTestClient.post()
                .uri("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(firstRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .returnResult()
                .getResponseBody();

        // Vérification anti-NullPointerException
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        CreateUserRequest secondRequest = new CreateUserRequest("Favienne", "Flavienee", "fabienneelateleaC@gmail.com","password");
        //when
        restTestClient.put()
                .uri("/api/users/{id}", createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(secondRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(user -> {
                    assertThat(createdUser.getId()).isEqualTo(user.getId());
                });
    }

    @Test
    public void updateUser_should_return_404_when_user_not_found(){
        // Given — on insère un premier user
        CreateUserRequest secondRequest = new CreateUserRequest("Favienne", "Flavienee", "fabienneelateleaC@gmail.com","password");
        //when
        restTestClient.put()
                .uri("/api/users/{id}", 5)
                .contentType(MediaType.APPLICATION_JSON)
                .body(secondRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void deleteUser_should_remove_user_from_database(){
        // Given — on insère un premier user
        CreateUserRequest firstRequest = new CreateUserRequest("Favie", "Flavien", "fabienneelatelea@gmail.com","password");
        User createdUser =restTestClient.post()
                .uri("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(firstRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .returnResult()
                .getResponseBody();
        //when
        restTestClient.delete()
                .uri("/api/users/{id}", createdUser.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        // Then — vérifie que le user est bien supprimé en base
        assertThat(userRepository.findById(createdUser.getId())).isEmpty();
    }

    @Test
    public void deleteUser_should_return_404_when_user_not_found(){
        //when
        restTestClient.delete()
                .uri("/api/users/{id}", 5)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}
