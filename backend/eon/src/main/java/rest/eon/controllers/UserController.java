package rest.eon.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.UserDto;
import rest.eon.models.User;
import rest.eon.services.UserService;


@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    final private UserService userService;
    private final PasswordEncoder passwordEncoder;
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    @GetMapping()
    public User getUser() {
        return userService.getUserByEmail(SecurityUtil.getSessionUser()).get();
    }

    @PutMapping()
    ResponseEntity<User> editUser(@Valid @RequestBody UserDto newUser) {
        String email = SecurityUtil.getSessionUser();
        return userService.getUserByEmail(email).map(user -> {
            user.setNickname(newUser.getNickname());
            user.setFirstname(newUser.getFirstname());
            user.setLastname(newUser.getLastname());
            user.setPassword(passwordEncoder.encode(newUser.getPassword()));
            logger.info("Task with id " + user.getId() + " has been updated!");
            return ResponseEntity.ok(userService.save(user));
        }).get();
    }

    @DeleteMapping()
    ResponseEntity<?> deleteUser() {
        userService.deleteByEmail(SecurityUtil.getSessionUser());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}