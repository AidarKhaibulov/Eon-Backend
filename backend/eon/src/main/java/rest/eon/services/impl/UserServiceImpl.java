package rest.eon.services.impl;

import org.springframework.stereotype.Service;
import rest.eon.models.User;
import rest.eon.repositories.UserRepository;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getUserIdByEmail(String email) {
        return userRepository.getFirstByEmail(email);
    }
}