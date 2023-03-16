package rest.eon.services.impl;

import org.springframework.stereotype.Service;
import rest.eon.models.User;
import rest.eon.repositories.UserRepository;
import rest.eon.services.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
