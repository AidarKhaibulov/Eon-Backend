package rest.eon.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        AuthenticationResponse data = service.register(request);
        if (data == null)
            return new ResponseEntity<>("Email or nickname is already taken!", HttpStatus.FORBIDDEN);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/authenticate")
    ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/isTokenValid/{token}")
    ResponseEntity<Boolean> isAuthenticated(@PathVariable String token) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(token));
            return ResponseEntity.ok(jwtService.isTokenValid(token, userDetails));
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}