package rest.eon.services;

import org.springframework.stereotype.Service;
import rest.eon.models.User;

import java.util.List;


public interface UserService {
    List<User> getAll();
    User getByLogin(String login);
}
