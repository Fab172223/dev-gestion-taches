package fab.nkomoelate.taskmanagement.integration;

import fab.nkomoelate.taskmanagement.security.AuthResponse;
import fab.nkomoelate.taskmanagement.security.LoginRequest;
import fab.nkomoelate.taskmanagement.security.RegisterRequest;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportTestcontainers(TestContainersConfig.class)
class SecurityIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    // -----------------------------------------------
    // Register
    // -----------------------------------------------

    @Test
    void register_should_return_201_with_token() {
        // When / Then
        AuthResponse response = restTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterRequest("Dupont", "Marie", "marie@example.com", "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.token()).isNotNull();
        assertThat(response.token()).isNotBlank();
    }

    // -----------------------------------------------
    // Login
    // -----------------------------------------------

    @Test
    void login_should_return_200_with_token_when_credentials_are_valid() {
        // Given — on crée d'abord un user
        restTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterRequest("Dupont", "Marie", "marie@example.com", "password123"))
                .exchange()
                .expectStatus().isCreated();

        // When / Then — on se connecte
        AuthResponse response = restTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest("marie@example.com", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.token()).isNotNull();
    }

    @Test
    void login_should_return_401_when_credentials_are_wrong() {
        // Given — on crée un user
        restTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterRequest("Dupont", "Marie", "marie@example.com", "password123"))
                .exchange()
                .expectStatus().isCreated();

        // When / Then — mauvais password
        restTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest("marie@example.com", "mauvaispassword"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // -----------------------------------------------
    // Accès endpoints protégés
    // -----------------------------------------------

    @Test
    void protected_endpoint_should_return_401_without_token() {
        // When / Then — pas de token → 401
        restTestClient.get()
                .uri("/api/users/")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protected_endpoint_should_return_200_with_valid_token() {
        // Given — on register et on récupère le token
        AuthResponse auth = restTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterRequest("Dupont", "Marie", "marie@example.com", "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(auth).isNotNull();

        // When / Then — avec le token → 200
        restTestClient.get()
                .uri("/api/users/")
                .header("Authorization", "Bearer " + auth.token())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void protected_endpoint_should_return_401_with_invalid_token() {
        // When / Then — token invalide → 401
        restTestClient.get()
                .uri("/api/users/")
                .header("Authorization", "Bearer token.invalide.ici")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void auth_endpoints_should_be_accessible_without_token() {
        // When / Then — /auth/** est public → pas besoin de token
        restTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterRequest("Test", "User", "test@test.com", "password123"))
                .exchange()
                .expectStatus().isCreated();
    }
}