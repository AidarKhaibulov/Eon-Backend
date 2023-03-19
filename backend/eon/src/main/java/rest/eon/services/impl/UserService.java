package rest.eon.services.impl;

import rest.eon.models.User;

import java.util.Optional;

public interface UserService {

    Optional<User> getUserIdByEmail(String email);

    Optional<User> getUserByEmail(String email);
}
