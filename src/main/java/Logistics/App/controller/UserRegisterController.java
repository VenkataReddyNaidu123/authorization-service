package Logistics.App.controller;

import Logistics.App.dto.LoginRequest;
import Logistics.App.dto.RefreshRequest;
import Logistics.App.dto.RegistrationRequest;
import Logistics.App.entity.User;
import Logistics.App.exception.InvalidCredentialsException;
import Logistics.App.exception.UserNotFoundException;
import Logistics.App.jwtConfig.JwtUtil;
import Logistics.App.repository.AuthorizationRepo;
import Logistics.App.service.RegistrationService;
import Logistics.App.service.TokenService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Locale;

@RestController
@RequestMapping("/logistics")
public class UserRegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private final RegistrationService registrationService;
    private final AuthorizationRepo authorizationRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    public UserRegisterController(RegistrationService registrationService,
                                  AuthorizationRepo authorizationRepo,
                                  PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenService tokenService) {
        this.registrationService = registrationService;
        this.authorizationRepo = authorizationRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> userRegistration(@Valid @RequestBody RegistrationRequest req){
        try {
            User user = new User();
            user.setUserName(req.getUserName());
            user.setEmail(req.getEmail());
            user.setPassword(req.getPassword());
            User saved = registrationService.saveUser(user);
            logger.info("User successfully registered");
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            logger.info("User unable to register");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshRequest req) {
        tokenService.findByToken(req.getRefreshToken()).ifPresent(tokenService::revokeRefreshToken);
        return ResponseEntity.ok().body("Logged out");
    }

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@Valid @RequestBody LoginRequest req) {

        String input = req.getUserNameOrEmail().trim();
        String password = req.getPassword();

        Optional<User> optionalUser= input.contains("@") ?
                authorizationRepo.findByEmail(input.toLowerCase(Locale.ROOT))
                :authorizationRepo.findByUserName(input);

        if (optionalUser.isEmpty()) {
            logger.warn("User entered Invalid Username or password");
            throw new UserNotFoundException("Invalid Username or password");
        }

        User storedUser = optionalUser.get();
        if (storedUser.getPassword() == null || storedUser.getPassword().isBlank()) {
            logger.info("Unable to login due to server unavailable");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: stored password missing");
        }
        if (!passwordEncoder.matches(password, storedUser.getPassword())) {
            logger.warn("User entered incorrect password");
            throw new InvalidCredentialsException("Incorrect password");
        }

        String token = jwtUtil.generateToken(storedUser.getUserName(), storedUser.getRole());
        var body = java.util.Map.of(
                "token", token,
                "tokenType", "Bearer"
        );
        return ResponseEntity.ok(body);

    }
}