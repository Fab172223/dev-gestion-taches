package fab.nkomoelate.taskmanagement.service;

import fab.nkomoelate.taskmanagement.controllers.UserController.*;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import fab.nkomoelate.taskmanagement.services.UserService;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
}
