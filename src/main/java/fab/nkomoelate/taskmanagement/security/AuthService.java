package fab.nkomoelate.taskmanagement.security;

import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // -----------------------------------------------
    // Register
    // -----------------------------------------------
    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .lastName(request.lastName())
                .firstName(request.firstName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new TaskManagementException.EmailAlreadyExistsException(request.email());
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);

    }

    // -----------------------------------------------
    // Login
    // -----------------------------------------------
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}