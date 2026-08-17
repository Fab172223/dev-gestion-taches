package fab.nkomoelate.taskmanagement.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtService jwtS;

    @MockitoBean
    UserDetailsServiceImpl userDI;
    @Autowired
    MockMvc mockMvc;

    @Test
    void register_should_returned_code_201_when_user_created() throws Exception{
        //given
        when(authService.register(any())).thenReturn(new AuthResponse("fake-token"));
        //when/then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "lastName": "fabeyene",
                    "firstName": "fablene",
                    "email": "fab@fabi.fr",
                    "password": "password1234."
                }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())       // ← le champ token existe
                .andExpect(jsonPath("$.token").value("fake-token"));
    }

    @Test
    void register_should_return_code_400_when_email_is_invalid() throws Exception{
        //when/then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                           {
                                "lastName": "fabeyene",
                                "firstName": "fablene",
                                "email": "fabfabi.fr",
                                "password": "password1234."
                            }         
                        """
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist());
        verifyNoInteractions(authService);
    }

    @Test
    void register_should_return_code_400_when_password_is_less_than_8_characters() throws Exception{
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "lastName": "fabeyene",
                                "firstName": "fablene",
                                "email": "fab@fabi.fr",
                                "password": "passwo."
                            }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist());
        verifyNoInteractions(authService);
    }

    @Test
    void login_should_return_code_201_when_you_are_logged() throws Exception{
       //given
        when(authService.login(any())).thenReturn(new AuthResponse("fake-token"));
                //when/then
                mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                                "email": "fab@fabi.fr",
                                "password": "password1234."
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_should_return_code_401_when_you_are_not_logged() throws Exception{
        // Given
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        //when/then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                                "email": "fab@fabi.fr",
                                "password": "password1234."
                            }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }
}
