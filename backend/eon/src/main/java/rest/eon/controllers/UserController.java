package rest.eon.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.ProfileInfo;
import rest.eon.dto.UserDto;
import rest.eon.models.User;
import rest.eon.services.UserService;

import java.util.NoSuchElementException;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
@Tag(name = "Users", description = "Represents api methods for users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    final private UserService userService;
    private final PasswordEncoder passwordEncoder;
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    private static ResponseEntity<String> UserNotFound() {
        return new ResponseEntity<>("Such a user not found", HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Returns current user")
    @GetMapping()
    public User getUser() {
        return userService.getUserByEmail(SecurityUtil.getSessionUser()).get();
    }

    @Operation(summary = "Find user by nickname")
    @GetMapping("/findByName/{nickname}")
    public ResponseEntity<?> findUserByNickname(@PathVariable String nickname) {
        try {
            ProfileInfo  user = userService.findByNickname(nickname);
            log.info("User founded {}",user);
            return ResponseEntity.ok(user);
        }
        catch (NoSuchElementException e){
            log.warn("User with {} nickname is not found", nickname);
            return UserNotFound();
        }
    }
    @Operation(summary = "Find user by id")
    @GetMapping("/findById/{id}")
    public ResponseEntity<?> findUserById(@PathVariable String id) {
        try {
            ProfileInfo user = userService.findById(id);
            log.info("User founded {}",user);
            return ResponseEntity.ok(user);
        }
        catch (NoSuchElementException e){
            log.warn("User with {} id is not found", id);
            return UserNotFound();
        }
    }
    @Operation(summary = "Edit current user")
    @PutMapping()
    ResponseEntity<User> editUser(@Valid @RequestBody UserDto newUser) {
        String email = SecurityUtil.getSessionUser();
        return userService.getUserByEmail(email).map(user -> {
            user.setNickname(newUser.getNickname());
            user.setFirstname(newUser.getFirstname());
            user.setLastname(newUser.getLastname());
            user.setPhotosUrl(newUser.getPhotosUrl());
            user.setPassword(passwordEncoder.encode(newUser.getPassword()));
            logger.info("Task with id " + user.getId() + " has been updated!");
            return ResponseEntity.ok(userService.save(user));
        }).get();
    }

    @Operation(summary = "Delete current user")
    @DeleteMapping()
    ResponseEntity<?> deleteUser() {
        userService.deleteByEmail(SecurityUtil.getSessionUser());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}