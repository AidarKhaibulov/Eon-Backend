package rest.eon.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.AuthenticationRequest;
import rest.eon.auth.AuthenticationResponse;
import rest.eon.auth.AuthenticationService;
import rest.eon.auth.RegisterRequest;
import rest.eon.models.User;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request)
    {
        AuthenticationResponse data=service.register(request);
        if(data==null)
            return new ResponseEntity<>("Email or nickname is already taken!", HttpStatus.FORBIDDEN);
        return ResponseEntity.ok(data);
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

}