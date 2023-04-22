package rest.eon.services;

import rest.eon.models.User;

import java.util.Optional;

public interface UserService {

    Optional<User> getUserIdByEmail(String email);

    Optional<User> getUserByEmail(String email);

    User save(User user);

    void deleteByEmail(String email);

    Optional<User> getUserById(String userId);

    Optional<User> findByNickname(String nickname);
}
