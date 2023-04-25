package rest.eon.auth;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rest.eon.controllers.TaskController;
import rest.eon.repositories.UserRepository;
import rest.eon.models.User;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);

    public AuthenticationResponse register(RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent() ||
                userRepository.findByNickname(request.getNickname()).isPresent())
        {
            logger.error("email or nickname is already taken");
            return null;
        }
        var user = User.builder()
                .nickname(request.getNickname())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("UNVERIFIED")
                .build();
        System.out.println(user.toString());
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        logger.info("user registered successfully");
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(userRepository.findByEmail(request.getEmail()).get().getId())
                .nickname(user.getNickname())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(userRepository.findByEmail(request.getEmail()).get().getId())
                .nickname(user.getNickname())
                .build();
    }


}