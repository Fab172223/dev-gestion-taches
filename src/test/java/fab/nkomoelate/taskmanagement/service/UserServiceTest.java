package fab.nkomoelate.taskmanagement.service;

import fab.nkomoelate.taskmanagement.controllers.UserController.*;
import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import fab.nkomoelate.taskmanagement.services.UserService;

import static java.util.Optional.empty;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void addUser_should_return_user_with_correct_data() {
        //given
        CreateUserRequest userRequest = new CreateUserRequest("faille","taille","fablene@yahoo.fr");
        User user = User.builder()
                .id(1L)
                .lastName("faille")
                .firstName("taille")
                .email("fablene@yahoo.fr")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);
        //when
        User result = userService.addUser(userRequest);
        //then
        assertThat(result.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(result.getLastName()).isEqualTo(user.getLastName());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void addUser_should_call_only_once_with_no_extra_interactions() {
        //given
        CreateUserRequest userRequest = new CreateUserRequest("faille","taille","fablene@yahoo.fr");
        User user = User.builder()
                .id(1L)
                .lastName("faille")
                .firstName("taille")
                .email("fablene@yahoo.fr")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);
        //when
       userService.addUser(userRequest);
        //then
        verify(userRepository,times(1)).save(any(User.class));
    }

    @Test
    void addUser_should_return_exception_if_email_exists_in_bdd(){
        //given
        CreateUserRequest userRequest = new CreateUserRequest("aile","faire","der@fil.fr");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));
        // when / then
        assertThatThrownBy(() -> userService.addUser(userRequest)).isInstanceOf(TaskManagementException.EmailAlreadyExistsException.class);
    }

    @Test
    void mapperUser_should_map_all_fields_correctly(){
        //given
        CreateUserRequest userRequest = new CreateUserRequest( "test0",
                "test1",
                "f@gmail.com");
        //when
        User userToSave = this.userService.mapperUser(userRequest,new User());
        //then
        assertThat(userToSave.getEmail()).isEqualTo(userRequest.email());
        assertThat(userToSave.getLastName()).isEqualTo((userRequest.lastName()));
        assertThat(userToSave.getFirstName()).isEqualTo(userRequest.firstName());
    }

    @Test
    void getUser_should_return_exception_when_user_is_null(){
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when/then
        assertThatThrownBy(() -> userService.getUser(99L)).isInstanceOf(TaskManagementException.UserNotFoundException.class);
    }

    @Test
    void getUser_should_return_user_when_it_found(){
        //given
        User user = User.builder().id(1L).email("test@test.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        //when
        User result = userService.getUser(1L);
        //then
        assertThat(user.getEmail()).isEqualTo(result.getEmail());
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void getUsers_should_return_all_users(){
        //given
        User user = User.builder()
                .id(1L)
                .lastName("faille")
                .firstName("taille")
                .email("fablene@yahoo.fr")
                .build();
        when(userRepository.findAll()).thenReturn(List.of(user));

        // When
        List<User> result = userService.getUsers();

        //then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void updateUser_should_return_updated_user() {
        // Given
        User userBdd = User.builder()
                .id(1L)
                .firstName("toto")
                .lastName("tata")
                .email("toto@toto.tito")
                .build();
        CreateUserRequest request = new CreateUserRequest("tonton", "fred", "toto@toto.toto");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userBdd));
        when(userRepository.save(any(User.class))).thenReturn(userBdd);

        // When
        User result = userService.updateUser( request,1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }
    @Test
    void updateUser_should_throw_UserNotFoundException_when_id_not_found(){
        //given
        CreateUserRequest request = new CreateUserRequest("tonton", "fred", "toto@toto.toto");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        //when/then
        assertThatThrownBy(() -> userService.updateUser(request,1L)).isInstanceOf(TaskManagementException.UserNotFoundException.class);
    }
    @Test
    void updateUser_should_throw_EmailAlreadyExistsException_when_email_duplicated() {
        // Given
        User userBdd = User.builder().id(4L).email("old@email.com").build();
        CreateUserRequest request = new CreateUserRequest("tonton", "fred", "existing@email.com");
        when(userRepository.findById(4L)).thenReturn(Optional.of(userBdd));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        // When / Then
        assertThatThrownBy(() -> userService.updateUser(request, 4L))
                .isInstanceOf(TaskManagementException.EmailAlreadyExistsException.class);
    }
    @Test
    void deleteUser_should_delete_existing_user() {
        // Given
        User user = User.builder().id(1L).email("test@test.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_should_throw_UserNotFoundException_when_id_not_found() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(TaskManagementException.UserNotFoundException.class);
    }
}
