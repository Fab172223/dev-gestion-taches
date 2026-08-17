package fab.nkomoelate.taskmanagement.security;

import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import fab.nkomoelate.taskmanagement.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_should_return_user_if_user_found(){
        //given
        User userTest = User.builder()
                .id(1L)
                .lastName("Dupont")
                .firstName("Marie")
                .email("marie@example.com")
                .password("hashedPassword")
                .build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(userTest));
        //when
        var result = userDetailsService.loadUserByUsername("marie@example.com");
        //then
        assertThat(result.getUsername()).isEqualTo("marie@example.com");
    }

    @Test
    void loadUserByUsername_should_throw_UsernameNotFoundException_when_email_not_found(){
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        //then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("marie@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

}
