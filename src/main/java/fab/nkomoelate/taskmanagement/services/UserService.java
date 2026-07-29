package fab.nkomoelate.taskmanagement.services;

import fab.nkomoelate.taskmanagement.controllers.UserController.*;
import fab.nkomoelate.taskmanagement.controllers.UserController;
import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public User addUser(CreateUserRequest user){
        User userToCreate = new User();
        try{
            return userRepository.save(mapperUser(user, userToCreate));
        }catch(DataIntegrityViolationException e){
            throw new TaskManagementException.EmailAlreadyExistsException(user.email());
        }
    }

    public User getUser(Long id){
            return userRepository.findById(id)
                    .orElseThrow(() -> new TaskManagementException.UserNotFoundException(id));
    }

    public User updateUser(CreateUserRequest userRequest, Long id) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new TaskManagementException.UserNotFoundException(id));
        try {
            return userRepository.save(mapperUser(userRequest, userToUpdate));
        } catch (DataIntegrityViolationException e) {
            throw new TaskManagementException.EmailAlreadyExistsException(userRequest.email());
        }
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public void deleteUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new TaskManagementException.UserNotFoundException(id));
        userRepository.deleteById(user.getId());
    }

    public User mapperUser(CreateUserRequest user, User userToCreate){
        userToCreate.setEmail(user.email());
        userToCreate.setFirstName(user.firstName());
        userToCreate.setLastName(user.lastName());
        userToCreate.setPassword(passwordEncoder.encode(user.password()));
        return userToCreate;
    }
}
