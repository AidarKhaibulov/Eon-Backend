package rest.eon.services;

import rest.eon.dto.ProfileInfo;
import rest.eon.models.User;
import rest.eon.services.impl.UserServiceImpl;

import java.util.Optional;

public interface UserService {

    Optional<User> getUserIdByEmail(String email);

    Optional<User> getUserByEmail(String email);

    User save(User user);

    void deleteByEmail(String email);

    Optional<User> getUserById(String userId);

   ProfileInfo findByNickname(String nickname);

    ProfileInfo findById(String id);
}
