package fab.nkomoelate.taskmanagement.services;

import fab.nkomoelate.taskmanagement.controllers.UserController.*;
import fab.nkomoelate.taskmanagement.controllers.UserController;
import fab.nkomoelate.taskmanagement.exceptions.TaskManagementException;
import fab.nkomoelate.taskmanagement.model.User;
import fab.nkomoelate.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User addUser(CreateUserRequest user){
        User userToCreate = new User();
        try{
            return userRepository.save(mapperUser(user, userToCreate));
        }catch(DataIntegrityViolationException e){
            throw new TaskManagementException.EmailAlreadyExistsException(user.email());
        }


    }
    public User mapperUser(CreateUserRequest user, User userToCreate){
        userToCreate.setEmail(user.email());
        userToCreate.setFirstName(user.firstName());
        userToCreate.setLastName(user.lastName());
        return userToCreate;
    }
}
