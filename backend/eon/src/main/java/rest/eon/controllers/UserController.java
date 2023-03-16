package rest.eon.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import rest.eon.models.User;
import rest.eon.services.UserService;

import java.util.List;

@RestController
public class UserController {
    final private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/users")
    public @ResponseBody List<User> getAll() {
        return userService.getAll();
    }


}
