package fab.nkomoelate.taskmanagement.security;

import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // -----------------------------------------------
    // register()
    // -----------------------------------------------

    @Test
    void register_should_return_token_when_successful() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "Dupont", "Marie", "marie@example.com", "password123");

        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(jwtService.generateToken(any())).thenReturn("fake-token");

        // When
        AuthResponse result = authService.register(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("fake-token");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_should_throw_EmailAlreadyExistsException_when_email_duplicated() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "Dupont", "Marie", "marie@example.com", "password123");

        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        // When / Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(TaskManagementException.EmailAlreadyExistsException.class);
    }

    // -----------------------------------------------
    // login()
    // -----------------------------------------------

    @Test
    void login_should_return_token_when_credentials_are_valid() {
        // Given
        LoginRequest request = new LoginRequest("marie@example.com", "password123");

        User userInDb = User.builder()
                .id(1L)
                .email("marie@example.com")
                .password("hashedPassword")
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        userInDb, null, userInDb.getAuthorities()));
        when(userRepository.findByEmail("marie@example.com"))
                .thenReturn(Optional.of(userInDb));
        when(jwtService.generateToken(any())).thenReturn("fake-token");

        // When
        AuthResponse result = authService.login(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("fake-token");
    }

    @Test
    void login_should_throw_BadCredentialsException_when_credentials_are_wrong() {
        // Given
        LoginRequest request = new LoginRequest("marie@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        // Le repository et JwtService ne doivent jamais être appelés
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }
}
