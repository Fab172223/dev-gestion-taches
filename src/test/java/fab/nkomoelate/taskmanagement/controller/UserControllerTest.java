package fab.nkomoelate.taskmanagement.controller;

import fab.nkomoelate.taskmanagement.controllers.UserController;
import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.security.JwtService;
import fab.nkomoelate.taskmanagement.security.UserDetailsServiceImpl;
import fab.nkomoelate.taskmanagement.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ============================================================

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

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
                                                "email" : "goMyLove@toto.com",
                                    "password": "test1234"
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
                                    "email": "marie@example.com",
                                    "password": "test1234"
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
                                    "email": null,
                                    "password": "test1234"
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
                                    "email": "ceci-nest-pas-un-email",
                                    "password": "test1234"
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
                                    "email": "marie@example.com",
                                    "password": "test1234"
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
                                    "email": "marie@example.com",
                                    "password": "test1234"
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
                            "email":"marie@example.com",
                                    "password": "test1234"
                        }
                        """
                )
        ).andExpect(status().isConflict());
    }

    @Test
    void getUser_should_return_200_if_user_exists() throws Exception{
        //given
        User userTest = User.builder()
                .id(1L)
                .firstName("ody")
                .lastName("eury")
                .email("keep@open.com")
                .build();
        when(userService.getUser(anyLong())).thenReturn(userTest);

        //when/then
        mockMvc.perform(get("/api/users/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("keep@open.com"));
    }

    @Test
    void getUser_should_return_404_if_user_not_found() throws Exception{
        //given
        when(userService.getUser(1L)).thenThrow(new TaskManagementException.UserNotFoundException(1L));
        //when/then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsers_should_return_ok() throws Exception{
        //when
        when(userService.getUsers()).thenReturn(new ArrayList<>());
        //then
        mockMvc.perform(get("/api/users/"))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_should_return_200_with_empty_list() throws Exception {
        // Given
        when(userService.getUsers()).thenReturn(new ArrayList<>());

        // When / Then
        mockMvc.perform(get("/api/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUsers_should_return_200_with_user_list() throws Exception {
        // Given
        when(userService.getUsers()).thenReturn(List.of(
                User.builder().id(1L).email("marie@example.com").build()
        ));

        // When / Then
        mockMvc.perform(get("/api/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("marie@example.com"));
    }

    // -----------------------------------------------
    // PUT /api/users/{id}
    // -----------------------------------------------

    @Test
    void updateUser_should_return_200_with_updated_user() throws Exception {
        // Given
        User updatedUser = User.builder()
                .id(1L)
                .lastName("Dupont")
                .firstName("Marie")
                .email("marie@example.com")
                .build();
        when(userService.updateUser(any(), eq(1L))).thenReturn(updatedUser);

        // When / Then
        mockMvc.perform(put("/api/users/{id}",1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "lastName": "Dupont",
                                    "firstName": "Marie",
                                    "email": "marie@example.com",
                                    "password": "test1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("marie@example.com"));
    }

    @Test
    void updateUser_should_return_404_when_user_not_found() throws Exception {
        // Given
        when(userService.updateUser(any(), eq(1L)))
                .thenThrow(new TaskManagementException.UserNotFoundException(1L));

        // When / Then
        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "lastName": "Dupont",
                                    "firstName": "Marie",
                                    "email": "marie@example.com",
                                    "password": "test1234"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_should_return_409_when_email_duplicated() throws Exception {
        // Given
        when(userService.updateUser(any(), eq(1L)))
                .thenThrow(new TaskManagementException.EmailAlreadyExistsException("marie@example.com"));

        // When / Then
        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "lastName": "Dupont",
                                    "firstName": "Marie",
                                    "email": "marie@example.com",
                                    "password": "test1234"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    // -----------------------------------------------
    // DELETE /api/users/{id}
    // -----------------------------------------------

    @Test
    void deleteUser_should_return_204_when_user_deleted() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When / Then
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_should_return_404_when_user_not_found() throws Exception {
        // Given
        doThrow(new TaskManagementException.UserNotFoundException(1L))
                .when(userService).deleteUser(1L);

        // When / Then
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNotFound());
    }
}
