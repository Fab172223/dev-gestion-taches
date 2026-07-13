package fab.nkomoelate.taskmanagement.controller;

import fab.nkomoelate.taskmanagement.controllers.UserController;
import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ============================================================

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addUser_should_returned_201_with_user_created() throws Exception {
        //given
        User userReturned = User.builder()
                .id(1L)
                .lastName("far")
                .firstName("away")
                .email("goMyLove@toto.com")
                .build();
        when(userService.addUser(any())).thenReturn(userReturned);

        //when / then
        mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                            {
                                                "lastName" : "far",
                                                "firstName" : "away",
                                                "email" : "goMyLove@toto.com"
                                            }
                                        """
                        ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.lastName").value("far"))
                .andExpect(jsonPath("$.firstName").value("away"))
                .andExpect(jsonPath("$.email").value("goMyLove@toto.com"));
    }

    @Test
    void addUser_should_call_service_exactly_once() throws Exception {
        // Given
        when(userService.addUser(any())).thenReturn(new User());

        // When
        mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "lastName": "Dupont",
                                    "firstName": "Marie",
                                    "email": "marie@example.com"
                                }
                                """))
                .andExpect(status().isCreated());

        // Then
        verify(userService, times(1)).addUser(any());
    }

    // -----------------------------------------------
    // Validation @Valid — email
    // C'est ici que @NotNull et @Email sont réellement
    // testés, car @Valid est déclenché par Spring
    // au moment de la désérialisation HTTP.
    // -----------------------------------------------

    @Test
    void addUser_should_return_400_when_email_is_null() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "lastName": "Dupont",
                                    "firstName": "Marie",
                                    "email": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        // Le service ne doit jamais être appelé si la validation échoue
        verifyNoInteractions(userService);
    }

    @Test
    void addUser_should_return_400_when_email_format_is_invalid() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "lastName": "Dupont",
                                    "firstName": "Marie",
                                    "email": "ceci-nest-pas-un-email"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // -----------------------------------------------
    // Erreurs techniques
    // -----------------------------------------------

    @Test
    void addUser_should_return_415_when_content_type_is_missing() throws Exception {
        // When / Then — Content-Type: application/json absent
        mockMvc.perform(post("/api/users/")
                        .content("""
                                {
                                    "lastName": "Dupont",
                                    "firstName": "Marie",
                                    "email": "marie@example.com"
                                }
                                """))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void addUser_should_return_500_when_service_throws_exception() throws Exception {
        // Given
        when(userService.addUser(any()))
                .thenThrow(new RuntimeException("Erreur inattendue"));

        // When / Then
        mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "lastName": "Dupont",
                                    "firstName": "Marie",
                                    "email": "marie@example.com"
                                }
                                """))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addUser_should_return_409_when_mail_duplicated() throws Exception {
        //given
        when(userService.addUser(any())).thenThrow(new TaskManagementException.EmailAlreadyExistsException("marie@example.com"));
        //when
        mockMvc.perform(post("/api/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {
                            "lastName":"Robert",
                            "firstName":"Loic",
                            "email":"marie@example.com"
                        }
                        """
                )
        ).andExpect(status().isConflict());
    }
}
