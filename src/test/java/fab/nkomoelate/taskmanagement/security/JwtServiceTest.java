package fab.nkomoelate.taskmanagement.security;

import fab.nkomoelate.taskmanagement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class JwtServiceTest {

    private JwtService jwtService;
    private User userTest;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);

        userTest = User.builder()
                .id(1L)
                .lastName("Dupont")
                .firstName("Marie")
                .email("marie@example.com")
                .password("hashedPassword")
                .build();
    }

    @Test
    void generateToken_should_return_token_when_user_is_authenticated(){
        //given
        String token = jwtService.generateToken(userTest);
        //when-then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_should_return_token_with_three_parts(){
        //given
        String token = jwtService.generateToken(userTest);
        //when-then
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractEmail_should_return_an_correct_email(){
        //given
        String token = jwtService.generateToken(userTest);
        //when
        String email = jwtService.extractEmail(token);
        //then
        assertThat(email).isNotNull();
        assertThat(email).isNotBlank();
        assertThat(email).isEqualTo("marie@example.com");
    }

    @Test
    void isTokenValid_should_return_true_when_token_is_valid(){
        //given
        String token = jwtService.generateToken(userTest);
        //when
        boolean validate = jwtService.isTokenValid(token, userTest);
        //then
        assertThat(validate).isEqualTo(true);
    }

    @Test
    void isTokenValid_should_return_false_when_token_is_with_another_user(){
        //given
        String token = jwtService.generateToken(userTest);
        User another = User.builder()
                .id(2L)
                .lastName("Dupont")
                .firstName("Marie")
                .email("mariel@example.com")
                .password("hashedPassword1")
                .build();
        //when
        boolean validate = jwtService.isTokenValid(token, another);
        //then
        assertThat(validate).isEqualTo(false);
    }

    @Test
    void istokenValid_should_return_false_when_token_expired(){
        //given
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "expiration", 0L);
        String token = jwtService.generateToken(userTest);
        // When / Then — le token est expiré → isTokenValid doit lever une exception
        // ou retourner false selon l'implémentation
        assertThatThrownBy(() -> jwtService.isTokenValid(token, userTest))
                .isInstanceOf(Exception.class);
    }


}
